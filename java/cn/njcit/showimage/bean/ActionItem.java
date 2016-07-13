package cn.njcit.showimage.bean;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

public class ActionItem {//ActionBar中的某个菜单项
	private Drawable icon;//图标
	private String actionName;//名字
	private OnClickListener onClickListener;//点击事件

	public ActionItem(Drawable img, String name, OnClickListener listener) {
		icon = img;
		actionName = name;
		this.onClickListener = listener;
	}

	public Drawable getIcon() {
		return icon;
	}

	public String getActionName() {
		return actionName;
	}

	public OnClickListener getOnClickListener() {
		return this.onClickListener;
	}

}