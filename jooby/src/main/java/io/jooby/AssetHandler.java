package io.jooby;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.List;

public class AssetHandler implements Route.Handler {
  private final AssetSource[] sources;

  private boolean etag = true;

  private boolean lastModified = true;

  private long maxAge = -1;

  private String filekey;

  public AssetHandler(AssetSource... sources) {
    this.sources = sources;
  }

  @Nonnull @Override public Object apply(@Nonnull Context ctx) throws Exception {
    String filepath = ctx.pathMap().get(filekey);
    Asset asset = resolve(filepath);
    if (asset == null) {
      ctx.sendStatusCode(StatusCode.NOT_FOUND);
      return ctx;
    }

    // handle If-None-Match
    if (this.etag) {
      String ifnm = ctx.header("If-None-Match").value((String) null);
      if (ifnm != null && ifnm.equals(asset.etag())) {
        ctx.sendStatusCode(StatusCode.NOT_MODIFIED);
        asset.release();
        return ctx;
      } else {
        ctx.header("ETag", asset.etag());
      }
    }

    // Handle If-Modified-Since
    if (this.lastModified) {
      long lastModified = asset.lastModified();
      if (lastModified > 0) {
        long ifms = ctx.header("If-Modified-Since").longValue(-1);
        if (lastModified <= ifms) {
          ctx.sendStatusCode(StatusCode.NOT_MODIFIED);
          asset.release();
          return ctx;
        }
        ctx.header("Last-Modified", new Date(lastModified));
      }
    }

    // cache max-age
    if (maxAge > 0) {
      ctx.header("Cache-Control", "max-age=" + maxAge);
    }

    long length = asset.length();
    if (length != -1) {
      ctx.length(length);
    }
    ctx.type(asset.type());
    return ctx.sendStream(asset.content());
  }

  public AssetHandler etag(boolean etag) {
    this.etag = etag;
    return this;
  }

  public AssetHandler lastModified(boolean lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  public AssetHandler maxAge(long maxAge) {
    this.maxAge = maxAge;
    return this;
  }

  private Asset resolve(String filepath) {
    for (AssetSource source : sources) {
      Asset asset = source.resolve(filepath);
      if (asset != null) {
        return asset;
      }
    }
    return null;
  }

  void route(Route route) {
    List<String> keys = route.pathKeys();
    this.filekey = keys.size() == 0 ? "*" : keys.get(0);
  }
}
