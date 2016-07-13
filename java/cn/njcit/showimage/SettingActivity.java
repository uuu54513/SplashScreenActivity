package cn.njcit.showimage;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.io.File;

import cn.njcit.showimage.meta.MetaData;

public class SettingActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.setting_preference);
		initListPreference();
	}
	
	private void initListPreference() {
		// getPreferenceManager������ѡ�����������
		@SuppressWarnings("deprecation")
		PreferenceManager manager = getPreferenceManager();
		// ����android:key��ָ�������ƣ��൱��id������ȡ��ѡ��
		ListPreference listPreference = (ListPreference) manager
				.findPreference("listPreference");

		int count = MetaData.albums.size();
		CharSequence[] entries = new CharSequence[count];
		CharSequence[] entryValues = new CharSequence[count];

		for (int i = 0; i < count; i++) {
			File f = new File(MetaData.albums.get(i).path);
			String fName = f.getName();
			entries[i] = fName;
			entryValues[i] = MetaData.albums.get(i).path;
		}
		
		listPreference.setEntries(entries);
		listPreference.setEntryValues(entryValues);
		
		
	}
}
