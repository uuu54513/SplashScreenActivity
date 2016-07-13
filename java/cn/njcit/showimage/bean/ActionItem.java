package cn.njcit.showimage.bean;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

public class ActionItem {//ActionBar�е�ĳ���˵���
	private Drawable icon;//ͼ��
	private String actionName;//����
	private OnClickListener onClickListener;//����¼�

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