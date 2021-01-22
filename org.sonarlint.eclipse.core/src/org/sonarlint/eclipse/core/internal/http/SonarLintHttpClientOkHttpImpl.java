package org.sonarlint.eclipse.core.internal.http;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class SonarLintHttpClientOkHttpImpl implements SonarLintHttpClient {
  private final OkHttpClient okClient;

  public SonarLintHttpClientOkHttpImpl(OkHttpClient okClient) {
    this.okClient = okClient;
  }

  @Override
  public Response post(String url, String contentType, String bodyContent) {
    RequestBody body = RequestBody.create(MediaType.get(contentType), bodyContent);
    Request request = new Request.Builder()
      .url(url)
      .post(body)
      .build();
    return executeRequest(request);
  }

  @Override
  public Response get(String url) {
    Request request = new Request.Builder()
      .url(url)
      .build();
    return executeRequest(request);
  }

  @Override
  public Response delete(String url, String contentType, String bodyContent) {
    RequestBody body = RequestBody.create(MediaType.get(contentType), bodyContent);
    Request request = new Request.Builder()
      .url(url)
      .delete(body)
      .build();
    return executeRequest(request);
  }

  private Response executeRequest(Request request) {
    try {
      return wrap(okClient.newCall(request).execute());
    } catch (IOException e) {
      throw new IllegalStateException("Unable to execute request: " + e.getMessage(), e);
    }
  }

  private Response wrap(okhttp3.Response wrapped) {
    return new Response() {

      @Override
      public String url() {
        return wrapped.request().url().toString();
      }

      @Override
      public int code() {
        return wrapped.code();
      }

      @Override
      public void close() {
        wrapped.close();
      }

      @Override
      public String bodyAsString() {
        try (ResponseBody body = wrapped.body()) {
          return body.string();
        } catch (IOException e) {
          throw new IllegalStateException("Unable to read response body: " + e.getMessage(), e);
        }
      }

      @Override
      public InputStream bodyAsStream() {
        return wrapped.body().byteStream();
      }

      @Override
      public String toString() {
        return wrapped.toString();
      }
    };
  }
}
