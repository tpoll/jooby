package org.jooby.internal.elasticsearch;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.support.RestUtils;
import org.jooby.Request;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class EmbeddedHttpRequest extends RestRequest {

  private Request req;

  private byte[] content;

  private String path;

  private Map<String, String> params = new HashMap<>();

  public EmbeddedHttpRequest(final String prefix, final Request req) throws Exception {
    this.req = requireNonNull(req, "Request is required.");

    this.path = req.path().substring(prefix.length());
    int idx = this.path.indexOf('?');
    if (idx > 0) {
      RestUtils.decodeQueryString(path.substring(idx + 1), 0, params);
    }
    if (req.length() > 0) {
      content = req.body(byte[].class);
    } else {
      content = new byte[0];
    }
  }

  @Override
  public String param(final String key, final String defaultValue) {
    return params.getOrDefault(key, defaultValue);
  }

  @Override
  public boolean hasParam(final String key) {
    return param(key) != null;
  }

  @Override
  public String param(final String key) {
    return params.get(key);
  }

  @Override
  public Map<String, String> params() {
    return params;
  }

  @Override
  public Method method() {
    return Method.valueOf(req.method());
  }

  @Override
  public String uri() {
    return path;
  }

  @Override
  public String rawPath() {
    return path;
  }

  @Override
  public boolean hasContent() {
    return content.length > 0;
  }

  @Override
  public boolean contentUnsafe() {
    return false;
  }

  @Override
  public BytesReference content() {
    return new BytesArray(content);
  }

  @Override
  public String header(final String name) {
    return req.header(name).toOptional().orElse(null);
  }

  @Override
  public Iterable<Entry<String, String>> headers() {
    ImmutableList.Builder<Entry<String, String>> headers = ImmutableList.builder();

    req.headers().forEach((k, v) -> headers.add(Maps.immutableEntry(k, v.value())));

    return headers.build();
  }

}
