package com.taryis.simpliui.vector;

public class LBAConstrainedVectorParser extends LBAVectorParser {

  private float originalWidth, originalHeight;
  private float viewWidth, viewHeight;

  private LBAConstrainedVectorParser(float originalWidth, float originalHeight, float viewWidth,
                                     float viewHeight) {
    this.originalWidth = originalWidth;
    this.originalHeight = originalHeight;
    this.viewWidth = viewWidth;
    this.viewHeight = viewHeight;
  }

  @Override protected float transformX(float x) {
    return x * (float)viewWidth / (float)originalWidth;
  }

  @Override protected float transformY(float y) {
    return y * (float)viewHeight / (float)originalHeight;
  }

  public static class Builder {

    private float originalWidth, originalHeight;
    private float viewWidth, viewHeight;

    public Builder originalWidth(float originalWidth) {
      this.originalWidth = originalWidth;
      return this;
    }

    public Builder originalHeight(float originalHeight) {
      this.originalHeight = originalHeight;
      return this;
    }

    public Builder viewWidth(float width) {
      this.viewWidth = width;
      return this;
    }

    public Builder viewHeight(float height) {
      this.viewHeight = height;
      return this;
    }

    public LBAConstrainedVectorParser build() {
      return new LBAConstrainedVectorParser(originalWidth, originalHeight, viewWidth, viewHeight);
    }
  }
}
