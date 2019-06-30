/*
 * Copyright (C) 2013 youten
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
package jp.co.ricoh.hmp.test.util;

/** BLE UUID Strings */
public class BleUuid {
	// 180A Device Information
	public static final String SERVICE_DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
	public static final String CHAR_MANUFACTURER_NAME_STRING = "00002a29-0000-1000-8000-00805f9b34fb";
	public static final String CHAR_MODEL_NUMBER_STRING = "00002a24-0000-1000-8000-00805f9b34fb";
	public static final String CHAR_SERIAL_NUMBER_STRING = "00002a25-0000-1000-8000-00805f9b34fb";

	// CustomService
	public static final String SERVICE_SAMPLE = "0000fff0-0000-1000-8000-00805f9b34fb";
	public static final String CHAR_SAMPLE_RWN = "0000fff4-0000-1000-8000-00805f9b34fb";
	public static final String CHAR_SAMPLE_R = "0000fff1-0000-1000-8000-00805f9b34fb";

	// 180d Services
	public static final String DESCRIPTOR_NOTIFICATION_ENABLED_STATE = "0000fff1-0000-1000-8000-00805f9b34fb";

	// 180F Battery Service
	public static final String SERVICE_BATTERY_SERVICE = "0000180F-0000-1000-8000-00805f9b34fb";
	public static final String CHAR_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";
}
