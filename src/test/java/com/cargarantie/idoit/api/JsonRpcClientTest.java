package com.cargarantie.idoit.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.cargarantie.idoit.api.jsonrpc.Batch;
import com.cargarantie.idoit.api.jsonrpc.IdoitRequest;
import com.cargarantie.idoit.api.jsonrpc.JsonRpcRequest;
import com.cargarantie.idoit.api.jsonrpc.JsonRpcResponse;
import com.cargarantie.idoit.api.jsonrpc.LoginResponse;
import com.cargarantie.idoit.api.model.exception.IdoitException;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class JsonRpcClientTest extends TestResourceAccess {

  @Mock
  private JsonRestClientWrapper restClient;

  private JsonRpcClient client;


  @BeforeEach
  void beforeEach() {
    client = new JsonRpcClient(restClient, "apiKey");
  }

  @Test
  void login_shouldSendRequestWithBasicAuthCredentials_then_ChangeHeadersToToken() {
    class Headers {

      MultivaluedMap<String, Object> latestHeaders;
      MultivaluedMap<String, Object> sendHeaders;
      Object loginResponse;

      Object setLatest(InvocationOnMock invocation) {
        latestHeaders = invocation.getArgument(0);
        return null;
      }

      Object doLogin(InvocationOnMock invocation) {
        sendHeaders = latestHeaders;
        return loginResponse;
      }
    }

    Headers headers = new Headers();
    JsonRpcRequest expectedLoginRequest = new JsonRpcRequest("0", "idoit.login",
        Collections.singletonMap("apikey", "apiKey"));
    headers.loginResponse = new JsonRpcResponse("0", LoginResponse.builder()
        .sessionId("theNewAndSecretSessionId").build(), null);
    doAnswer(headers::setLatest).when(restClient).setAuthHeaders(any());
    doAnswer(headers::doLogin).when(restClient).send(expectedLoginRequest, JsonRpcResponse.class);

    client.login("username", "password");

    assertThat(headers.sendHeaders).isEqualTo(map("X-RPC-Auth-Username", "username",
        "X-RPC-Auth-Password", "password"));
    assertThat(headers.latestHeaders)
        .isEqualTo(map("X-RPC-Auth-Session", "theNewAndSecretSessionId"));
  }

  @Test
  void send_shouldWrapRequestInJsonRpc_and_sendItToRestClientWrapper() {
    when(restClient.send(any(), any())).thenAnswer(this::getTestResponse);

    TestResponse response = client.send(new TestRequest(42));

    assertThat(response.stringValue).isEqualTo("42");
  }

  @Test
  void send_shouldSendBatchInOneJsonRpc_andReturnResponsesInRequestOrder() {
    when(restClient.send(any(), any())).thenAnswer(this::getTestResponseBatched);
    Batch<TestResponse> batch = new Batch<>();
    batch.add("42", new TestRequest(42));
    batch.add("41", new TestRequest(41));
    batch.add("99", new TestRequest(99));
    Map<String, TestResponse> response = client.send(batch);

    assertThat(response.keySet()).containsExactly("42", "41", "99");
    assertThat(response.values()).containsExactly(new TestResponse("42"),
        new TestResponse("41"), new TestResponse("99"));
  }

  @Test
  void send_shouldReturnEmptyMap_forEmptyBatch() {
    Map<String, TestResponse> response = client.send(new Batch<>());

    assertThat(response).isEmpty();
  }

  @Test
  void send_shouldThrowAnException_onErrorResponse() {
    when(restClient.send(any(), any())).thenAnswer(a -> new JsonRpcResponse("id", null,
        "error"));

    assertThatThrownBy(() -> client.send(new TestRequest(0)))
        .isInstanceOf(IdoitException.class)
        .hasMessage("Received error <error> for request"
            + " <JsonRpcClientTest.TestRequest(intValue=0)>");
  }

  @Test
  void send_shouldProperlyFormat_onLargeErrorResponses() {
    when(restClient.send(any(), any()))
        .thenAnswer(a -> getJson("constraintViolationErrorMessage", JsonRpcResponse.class));

    assertThatThrownBy(() -> client.send(new TestRequest(0)))
        .isInstanceOf(IdoitException.class)
        .hasMessage("Received error <{code=-32603, message=Internal error:"
            + " There was a validation error:"
            + " sysid(text): The given value is not unique and is already being used in other"
            + " objects: <ul class=\"m0 mt10 list-style-none\"><li><span>Virtual server » My"
            + " little server</span></li></ul>, data={sysid=The given value is not unique and"
            + " is already being used in other objects: <ul class=\"m0 mt10 list-style-none\">"
            + "<li><span>Virtual server » My little server</span></li></ul>}}>"
            + " for request <JsonRpcClientTest.TestRequest(intValue=0)>");
  }

  @Test
  void send_shouldThrowAnException_onNullResultResponse() {
    when(restClient.send(any(), any())).thenAnswer(a -> new JsonRpcResponse("id", null,
        null));

    assertThatThrownBy(() -> client.send(new TestRequest(0)))
        .isInstanceOf(IdoitException.class)
        .hasMessage("Result is null for request <JsonRpcClientTest.TestRequest(intValue=0)>");
  }

  private JsonRpcResponse getTestResponse(InvocationOnMock invocationOnMock) {
    JsonRpcRequest request = invocationOnMock.getArgument(0);
    return getResponse(request);
  }

  private JsonRpcResponse getResponse(JsonRpcRequest request) {
    return new JsonRpcResponse(request.getId(),
        new TestResponse(request.getParams().get("int_value").toString()), null);
  }

  private JsonRpcResponse[] getTestResponseBatched(InvocationOnMock invocationOnMock) {
    List<JsonRpcRequest> requests = invocationOnMock.getArgument(0);
    return requests.stream().map(this::getResponse).toArray(JsonRpcResponse[]::new);
  }

  private MultivaluedMap<String, Object> map(String... keyValue) {
    MultivaluedMap<String, Object> map = new MultivaluedHashMap<>();

    for (int i = 0; i < keyValue.length; i += 2) {
      map.add(keyValue[i], keyValue[i + 1]);
    }

    return map;
  }

  @Value
  private static class TestRequest implements IdoitRequest<TestResponse> {

    int intValue;

    @Override
    public Class<TestResponse> getResponseClass() {
      return TestResponse.class;
    }

    @Override
    public String getMethod() {
      return null;
    }
  }

  @Value
  private static class TestResponse {

    @JsonValue
    String stringValue;
  }
}
