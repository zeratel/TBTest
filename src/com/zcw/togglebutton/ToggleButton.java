package com.zcw.togglebutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.tbtest.R;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;

/**
 * @author ThinkPad
 * 
 *         add text by tmexcept 如果想向控件里添加文字，需要根据文字个数设置空间的宽高
 * 
 */
public class ToggleButton extends View {
	private SpringSystem springSystem;
	private Spring spring;
	/** */
	private float radius;
	/** 开启颜色 */
	private int onColor = Color.parseColor("#4ebb7f");
	/** 关闭颜色 */
	private int offBorderColor = Color.parseColor("#dadbda");
	/** 灰色带颜色 */
	private int offColor = Color.parseColor("#ffffff");
	/** 手柄颜色 */
	private int spotColor = Color.parseColor("#ffffff");
	/** 边框颜色 */
	private int borderColor = offBorderColor;
	/** 画笔 */
	private Paint paint;
	/** 开关状态 */
	private boolean toggleOn = false;
	/** 边框大小 */
	private int borderWidth = 2;
	/** 垂直中心 */
	private float centerY;
	/** 按钮的开始和结束位置 */
	private float startX, endX;
	/** 手柄X位置的最小和最大值 */
	private float spotMinX, spotMaxX;
	/** 手柄大小 */
	private int spotSize;
	/** 手柄X位置 */
	private float spotX;
	/** 关闭时内部灰色带高度 */
	private float offLineWidth;
	/**
	 * 手指距离
	 */
	private int fingerDistanceX;

	/**
	 * 移动点击锁
	 */
	private boolean isMove = false;
	
	/**
	 * 触摸感应距离
	 */
	private int touchDistance = 50;
	
	/**
	 * 内边距
	 */
	private int leftRightPadding = 8;

	/** */
	private RectF rect = new RectF();

	private OnToggleChanged listener;
	/**
	 * 选中后显示的文字
	 */
	private String selectText;
	/**
	 * 非选中显示的文字
	 */
	private String unSelectText;
	/**
	 * 文字颜色
	 */
	private int selectTextColor = Color.parseColor("#ffff5555"),
			unSelectTextColor = Color.parseColor("#ffff5555");
	/**
	 * 字体大小
	 */
	private float fontSize = 0;

	private ToggleButton(Context context) {
		super(context);
	}

	public ToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setup(attrs);
	}

	public ToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(attrs);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		spring.removeListener(springListener);
	}

	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		spring.addListener(springListener);
	}

	@SuppressLint("ClickableViewAccessibility")
	public void setup(AttributeSet attrs) {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Style.FILL);
		paint.setStrokeCap(Cap.ROUND);

		springSystem = SpringSystem.create();
		spring = springSystem.createSpring();
		spring.setSpringConfig(SpringConfig
				.fromOrigamiTensionAndFriction(50, 7));

		this.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				// toggle();
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					fingerDistanceX = (int) event.getX();
					break;
				case MotionEvent.ACTION_MOVE:
					if (toggleOn) {

						if (fingerDistanceX - (int) event.getX() > touchDistance) {
							toggle();
							fingerDistanceX = (int) event.getX();
							isMove = true;
						}
					} else {

						if ((int) event.getX() - fingerDistanceX > touchDistance) {
							toggle();
							fingerDistanceX = (int) event.getX();
							isMove = true;
						}
					}
					break;
				case MotionEvent.ACTION_UP:
					Log.i("LHF", "MotionEvent event");
					if (!isMove) {
						toggle();
					} else {
						isMove = false;
					}
					break;

				default:
					break;
				}

				return true;
			}
		});

		TypedArray typedArray = getContext().obtainStyledAttributes(attrs,
				R.styleable.ToggleButton);
		offBorderColor = typedArray.getColor(
				R.styleable.ToggleButton_offBorderColor, offBorderColor);
		onColor = typedArray
				.getColor(R.styleable.ToggleButton_onColor, onColor);
		spotColor = typedArray.getColor(R.styleable.ToggleButton_spotColor,
				spotColor);
		offColor = typedArray.getColor(R.styleable.ToggleButton_offColor,
				offColor);
		selectText = typedArray.getString(R.styleable.ToggleButton_selectText);
		unSelectText = typedArray
				.getString(R.styleable.ToggleButton_unSelectText);
		selectTextColor = typedArray.getColor(
				R.styleable.ToggleButton_selectTextColor, selectTextColor);
		unSelectTextColor = typedArray.getColor(
				R.styleable.ToggleButton_unSelectTextColor, unSelectTextColor);
		touchDistance = typedArray.getDimensionPixelSize(
				R.styleable.ToggleButton_touchDistance, touchDistance);
		borderWidth = typedArray.getDimensionPixelSize(
				R.styleable.ToggleButton_borderWidth, borderWidth);
		typedArray.recycle();
	}

	public void toggle() {
		toggleOn = !toggleOn;
		spring.setEndValue(toggleOn ? 1 : 0);
		if (listener != null) {
			listener.onToggle(toggleOn);
		}
	}

	public void toggleOn() {
		setToggleOn();
		if (listener != null) {
			listener.onToggle(toggleOn);
		}
	}

	public void toggleOff() {
		setToggleOff();
		if (listener != null) {
			listener.onToggle(toggleOn);
		}
	}

	/**
	 * 设置显示成打开样式，不会触发toggle事件
	 */
	public void setToggleOn() {
		toggleOn = true;
		spring.setEndValue(toggleOn ? 1 : 0);
	}

	/**
	 * 设置显示成关闭样式，不会触发toggle事件
	 */
	public void setToggleOff() {
		toggleOn = false;
		spring.setEndValue(toggleOn ? 1 : 0);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		final int width = getWidth();
		final int height = getHeight();
		radius = Math.min(width, height) * 0.5f;
		centerY = radius;
		startX = radius;
		endX = width - radius;
		spotMinX = startX + borderWidth;
		spotMaxX = endX - borderWidth;
		spotSize = height - 4 * borderWidth;
		spotX = spotMinX;
		offLineWidth = 0;
	}

	SimpleSpringListener springListener = new SimpleSpringListener() {
		@Override
		public void onSpringUpdate(Spring spring) {
			final double value = spring.getCurrentValue();

			final float mapToggleX = (float) SpringUtil
					.mapValueFromRangeToRange(value, 0, 1, spotMinX, spotMaxX);
			spotX = mapToggleX;

			float mapOffLineWidth = (float) SpringUtil
					.mapValueFromRangeToRange(1 - value, 0, 1, 10, spotSize);
			fontSize = (float) SpringUtil.mapValueFromRangeToRange(1 - value,
					0, 1, spotSize, 10);

			offLineWidth = mapOffLineWidth;

			final int fb = Color.blue(onColor);
			final int fr = Color.red(onColor);
			final int fg = Color.green(onColor);

			final int tb = Color.blue(offBorderColor);
			final int tr = Color.red(offBorderColor);
			final int tg = Color.green(offBorderColor);

			int sb = (int) SpringUtil.mapValueFromRangeToRange(1 - value, 0, 1,
					fb, tb);
			int sr = (int) SpringUtil.mapValueFromRangeToRange(1 - value, 0, 1,
					fr, tr);
			int sg = (int) SpringUtil.mapValueFromRangeToRange(1 - value, 0, 1,
					fg, tg);

			sb = SpringUtil.clamp(sb, 0, 255);
			sr = SpringUtil.clamp(sr, 0, 255);
			sg = SpringUtil.clamp(sg, 0, 255);

			borderColor = Color.rgb(sr, sg, sb);

			postInvalidate();
		}
	};

	@Override
	public void draw(Canvas canvas) {

		//
		rect.set(0 + leftRightPadding , 0 + leftRightPadding , getWidth()
				- leftRightPadding , getHeight() - leftRightPadding );
		// rect.set(0, 0, getWidth(), getHeight());
		paint.setColor(borderColor);
		canvas.drawRoundRect(rect, radius, radius, paint);

		if (offLineWidth > 0) {
			float cy = offLineWidth * 0.5f;
			// rect.set(spotX - cy, centerY - cy, endX + cy, centerY + cy);
			rect.set(spotX - cy + leftRightPadding , centerY - cy
					+ leftRightPadding , endX + cy - leftRightPadding , centerY
					+ cy - leftRightPadding );
			paint.setColor(offColor);
			canvas.drawRoundRect(rect, cy, cy, paint);

		}
		// ************draw text start************************
		Paint mTextPaint = new Paint();
		Rect mTextBound = new Rect();
		if (selectText != null) {
			mTextPaint.setTextSize((float) (fontSize * 0.5));
			mTextPaint.getTextBounds(selectText, 0, selectText.length(),
					mTextBound);
			mTextPaint.setColor(selectTextColor);
			canvas.drawText(selectText, spotMinX - mTextBound.width() * 1 / 4,
					(radius + mTextBound.height() / 3), mTextPaint);
		}
		if (unSelectText != null) {
			if (offLineWidth == 0) {
				offLineWidth = spotSize;
			}
			mTextPaint.setTextSize((float) (offLineWidth * 0.5));
			mTextPaint.getTextBounds(unSelectText, 0, unSelectText.length(),
					mTextBound);
			mTextPaint.setColor(unSelectTextColor);
			canvas.drawText(unSelectText,
					spotMaxX - mTextBound.width() * 3 / 4,
					(radius + mTextBound.height() / 3), mTextPaint);
		}
		// ************draw text end************************

		rect.set(spotX - 1 - radius + leftRightPadding , centerY - radius
				+ leftRightPadding , spotX + 1.1f + radius - leftRightPadding ,
				centerY + radius - leftRightPadding );
		paint.setColor(borderColor);
		canvas.drawRoundRect(rect, radius, radius, paint);

		final float spotR = spotSize * 0.5f;
		rect.set(spotX - spotR + leftRightPadding , centerY - spotR
				+ leftRightPadding , spotX + spotR - leftRightPadding , centerY
				+ spotR - leftRightPadding );
		paint.setColor(spotColor);
		canvas.drawRoundRect(rect, spotR, spotR, paint);

	}

	/**
	 * @author ThinkPad
	 * 
	 */
	public interface OnToggleChanged {
		/**
		 * @param on
		 */
		public void onToggle(boolean on);
	}

	public void setOnToggleChanged(OnToggleChanged onToggleChanged) {
		listener = onToggleChanged;
	}

	public int getLeftRightPadding() {
		return leftRightPadding;
	}

	public void setLeftRightPadding(int leftRightPadding) {
		this.leftRightPadding = leftRightPadding;
	}
	
}
