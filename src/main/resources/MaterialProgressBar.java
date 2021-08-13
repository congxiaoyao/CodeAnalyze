/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lingyue.bananalibrary.widgets.MaterialLoadingDialog;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.lingyue.bananalibrary.R;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

public class MaterialProgressBar extends ImageView {

  private static final int KEY_SHADOW_COLOR = 0x1E000000;
  private static final int FILL_SHADOW_COLOR = 0x3D000000;
  // PX
  private static final float X_OFFSET = 0f;
  private static final float Y_OFFSET = 1.75f;
  private static final float SHADOW_RADIUS = 3.5f;
  private static final int SHADOW_ELEVATION = 4;

  private static final int DEFAULT_CIRCLE_BG_LIGHT = 0xFFFAFAFA;
  private static final int DEFAULT_CIRCLE_DIAMETER = 56;
  private static final int STROKE_WIDTH_LARGE = 3;
  public static final int DEFAULT_TEXT_SIZE = 9;

  private float density;
  private Animation.AnimationListener listener;
  private int shadowRadius;
  private int backGroundColor;
  private int progressColor;
  private int progressStokeWidth;
  private int arrowWidth;
  private int arrowHeight;
  private int progress;
  private int max;
  private int innerRadius;
  private int textSize;
  private boolean ifDrawText;
  private boolean showArrow;
  private boolean circleBackgroundEnabled;
  private Paint textPaint;
  private MaterialProgressDrawable progressDrawable;
  private int[] colors = new int[]{Color.BLACK};
  private ShapeDrawable bgCircle = new ShapeDrawable(new OvalShape());
  private OvalShadow ovalShadow = new OvalShadow();

  public MaterialProgressBar(Context context) {
    super(context);
    init(context, null, 0);
  }

  public MaterialProgressBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  public MaterialProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr);
  }

  private void init(Context context, AttributeSet attrs, int defStyleAttr) {
    final TypedArray a = context.obtainStyledAttributes(
        attrs, R.styleable.MaterialProgressBar, defStyleAttr, 0);

    density = getContext().getResources().getDisplayMetrics().density;

    backGroundColor = a.getColor(
        R.styleable.MaterialProgressBar_background_color, DEFAULT_CIRCLE_BG_LIGHT);

    progressColor = a.getColor(
        R.styleable.MaterialProgressBar_progress_color, DEFAULT_CIRCLE_BG_LIGHT); //TODO 默认颜色

    innerRadius = a.getDimensionPixelOffset(
        R.styleable.MaterialProgressBar_inner_radius, -1);

    progressStokeWidth = a.getDimensionPixelOffset(
        R.styleable.MaterialProgressBar_progress_stoke_width, (int) (STROKE_WIDTH_LARGE * density));

    arrowWidth = a.getDimensionPixelOffset(
        R.styleable.MaterialProgressBar_arrow_width, -1);

    arrowHeight = a.getDimensionPixelOffset(
        R.styleable.MaterialProgressBar_arrow_height, -1);

    textSize = a.getDimensionPixelOffset(
        R.styleable.MaterialProgressBar_progress_text_size, (int) (DEFAULT_TEXT_SIZE * density));

    int textColor = a.getColor(
        R.styleable.MaterialProgressBar_progress_text_color, Color.BLACK);

    showArrow = a.getBoolean(R.styleable.MaterialProgressBar_show_arrow, false);

    circleBackgroundEnabled = a.getBoolean(R.styleable.MaterialProgressBar_enable_circle_background, true);

    progress = a.getInt(R.styleable.MaterialProgressBar_progress, 0);

    max = a.getInt(R.styleable.MaterialProgressBar_max, 100);

    int textVisible = a.getInt(R.styleable.MaterialProgressBar_progress_text_visibility, 1);
    if (textVisible != 1) {
      ifDrawText = true;
    }

    textPaint = new Paint();
    textPaint.setStyle(Paint.Style.FILL);
    textPaint.setColor(textColor);
    textPaint.setTextSize(textSize);
    textPaint.setAntiAlias(true);

    a.recycle();

    progressDrawable = new MaterialProgressDrawable(getContext(), this);
    super.setImageDrawable(progressDrawable);
  }

  private boolean elevationSupported() {
    return Build.VERSION.SDK_INT >= 21;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (!elevationSupported()) {
      setMeasuredDimension(getMeasuredWidth() + shadowRadius * 2,
          getMeasuredHeight() + shadowRadius * 2);
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    int diameter = Math.min(getMeasuredWidth(), getMeasuredHeight());
    if (diameter <= 0) {
      diameter = (int) density * DEFAULT_CIRCLE_DIAMETER;
    }

    if (getBackground() == null && circleBackgroundEnabled) {
      final int shadowYOffset = (int) (density * Y_OFFSET);
      final int shadowXOffset = (int) (density * X_OFFSET);
      shadowRadius = (int) (density * SHADOW_RADIUS);

      if (elevationSupported()) {
        ViewCompat.setElevation(this, SHADOW_ELEVATION * density);

      } else {
        ovalShadow.update(shadowRadius, diameter);
        bgCircle.setShape(ovalShadow);
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, bgCircle.getPaint());
        bgCircle.getPaint().setShadowLayer(shadowRadius, shadowXOffset, shadowYOffset, KEY_SHADOW_COLOR);
        // set padding so the inner image sits correctly within the shadow.
        setPadding(shadowRadius, shadowRadius, shadowRadius, shadowRadius);
      }

      bgCircle.getPaint().setColor(backGroundColor);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        setBackground(bgCircle);
      } else {
        setBackgroundDrawable(bgCircle);
      }
    }

    progressDrawable.setBackgroundColor(backGroundColor);
    progressDrawable.setColorSchemeColors(colors);
    progressDrawable.setSizeParameters(diameter, diameter,
        innerRadius <= 0 ? (diameter - progressStokeWidth * 2) / 4f : innerRadius,
        progressStokeWidth,
        arrowWidth < 0 ? progressStokeWidth * 4 : arrowWidth,
        arrowHeight < 0 ? progressStokeWidth * 2 : arrowHeight);

    if (isShowArrow()) {
      progressDrawable.setArrowScale(1f);
      progressDrawable.showArrow(true);
    }

    super.setImageDrawable(null);
    super.setImageDrawable(progressDrawable);
    progressDrawable.setAlpha(255);
    progressDrawable.start();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (ifDrawText) {
      String text = String.format("%s%%", progress);
      int x = getWidth() / 2 - text.length() * textSize / 4;
      int y = getHeight() / 2 + textSize / 4;
      canvas.drawText(text, x, y, textPaint);
    }
  }

  @Override
  final public void setImageResource(int resId) {

  }

  public boolean isShowArrow() {
    return showArrow;
  }

  public void setShowArrow(boolean showArrow) {
    this.showArrow = showArrow;
  }

  @Override
  final public void setImageURI(Uri uri) {
    super.setImageURI(uri);
  }

  @Override
  final public void setImageDrawable(Drawable drawable) {
  }

  public void setAnimationListener(Animation.AnimationListener listener) {
    this.listener = listener;
  }

  @Override
  public void onAnimationStart() {
    super.onAnimationStart();
    if (listener != null) {
      listener.onAnimationStart(getAnimation());
    }
  }

  @Override
  public void onAnimationEnd() {
    super.onAnimationEnd();
    if (listener != null) {
      listener.onAnimationEnd(getAnimation());
    }
  }

  public void setColorSchemeResources(int... colorResIds) {
    int[] colorRes = new int[colorResIds.length];
    for (int i = 0; i < colorResIds.length; i++) {
      colorRes[i] = ContextCompat.getColor(getContext(), colorResIds[i]);
    }
    setColorSchemeColors(colorRes);
  }

  public void setColorSchemeColors(int... colors) {
    this.colors = colors;
    if (progressDrawable != null) {
      progressDrawable.setColorSchemeColors(colors);
    }
  }

  public void setBackgroundColor(@ColorRes int colorRes) {
    if (getBackground() instanceof ShapeDrawable) {
      ((ShapeDrawable) getBackground()).getPaint()
          .setColor(ContextCompat.getColor(getContext(), colorRes));
    }
  }

  public boolean isShowProgressText() {
    return ifDrawText;
  }

  public void setShowProgressText(boolean mIfDrawText) {
    this.ifDrawText = mIfDrawText;
  }

  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public int getProgress() {
    return progress;
  }

  public void setProgress(int progress) {
    if (getMax() > 0) {
      this.progress = progress;
    }
  }

  public boolean circleBackgroundEnabled() {
    return circleBackgroundEnabled;
  }

  public void setCircleBackgroundEnabled(boolean enableCircleBackground) {
    this.circleBackgroundEnabled = enableCircleBackground;
  }

  @Override
  public int getVisibility() {
    return super.getVisibility();
  }

  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);
    if (progressDrawable != null) {
      if (visibility != VISIBLE) {
        progressDrawable.stop();
      }
      progressDrawable.setVisible(visibility == VISIBLE, false);
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (progressDrawable != null) {
      progressDrawable.start();
      progressDrawable.setVisible(getVisibility() == VISIBLE, false);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (progressDrawable != null) {
      progressDrawable.stop();
      progressDrawable.setVisible(false, false);
    }
  }

  private class OvalShadow extends OvalShape {
    private RadialGradient radialGradient;
    private Paint shadowPaint;
    private int shadowRadius;
    private int circleDiameter;

    public OvalShadow() {

    }

    public OvalShadow(int shadowRadius, int circleDiameter) {
      super();

      this.shadowRadius = shadowRadius;
      this.circleDiameter = circleDiameter;

      updateShader();
    }

    private void updateShader() {
      radialGradient = new RadialGradient(
          this.circleDiameter / 2f,
          this.circleDiameter / 2f,
          this.shadowRadius,
          new int[]{FILL_SHADOW_COLOR, Color.TRANSPARENT},
          null,
          Shader.TileMode.CLAMP);

      shadowPaint = new Paint();
      shadowPaint.setShader(radialGradient);
    }

    public void update(int shadowRadius, int circleDiameter) {
      if (this.shadowRadius != shadowRadius ||
          this.circleDiameter != circleDiameter) {
        this.shadowRadius = shadowRadius;
        this.circleDiameter = circleDiameter;
        updateShader();
      }
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      final int viewWidth = MaterialProgressBar.this.getWidth();
      final int viewHeight = MaterialProgressBar.this.getHeight();
      canvas.drawCircle(viewWidth / 2f, viewHeight / 2f, (circleDiameter / 2f + shadowRadius), shadowPaint);
      canvas.drawCircle(viewWidth / 2f, viewHeight / 2f, (circleDiameter / 2f), paint);
    }
  }
}
