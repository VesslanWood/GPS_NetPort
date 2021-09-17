/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api.sample;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class NetPortPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.net_port_preferences);

		// Devices
		final ListPreference ips = (ListPreference)findPreference("IP");
        String[] entries = Constants.NET_IPS;
        String[] entryValues = Constants.NET_IPS;
		ips.setEntries(entries);
		ips.setEntryValues(entryValues);
		ips.setSummary(ips.getValue());
		ips.setOnPreferenceChangeListener((preference, newValue) -> {
			preference.setSummary((String)newValue);
			return true;
		});

		final ListPreference ports = (ListPreference)findPreference("PORT");
		ports.setSummary(ports.getValue());
		ports.setOnPreferenceChangeListener((preference, newValue) -> {
			preference.setSummary((String)newValue);
			return true;
		});
	}
}
