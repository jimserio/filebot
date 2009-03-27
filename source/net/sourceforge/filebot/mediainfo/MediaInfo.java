
package net.sourceforge.filebot.mediainfo;


import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.sun.jna.Pointer;
import com.sun.jna.WString;


public class MediaInfo implements Closeable {
	
	private Pointer handle;
	
	
	public MediaInfo() {
		handle = MediaInfoLibrary.INSTANCE.New();
	}
	

	public boolean open(File file) {
		return MediaInfoLibrary.INSTANCE.Open(handle, new WString(file.getPath())) > 0;
	}
	

	public String inform() {
		return MediaInfoLibrary.INSTANCE.Inform(handle).toString();
	}
	

	public String option(String option) {
		return option(option, "");
	}
	

	public String option(String option, String value) {
		return MediaInfoLibrary.INSTANCE.Option(handle, new WString(option), new WString(value)).toString();
	}
	

	public String get(StreamKind streamKind, int streamNumber, String parameter) {
		return get(streamKind, streamNumber, parameter, InfoKind.Text, InfoKind.Name);
	}
	

	public String get(StreamKind streamKind, int streamNumber, String parameter, InfoKind infoKind) {
		return get(streamKind, streamNumber, parameter, infoKind, InfoKind.Name);
	}
	

	public String get(StreamKind streamKind, int streamNumber, String parameter, InfoKind infoKind, InfoKind searchKind) {
		return MediaInfoLibrary.INSTANCE.Get(handle, streamKind.ordinal(), streamNumber, new WString(parameter), infoKind.ordinal(), searchKind.ordinal()).toString();
	}
	

	public String get(StreamKind streamKind, int streamNumber, int parameterIndex) {
		return get(streamKind, streamNumber, parameterIndex, InfoKind.Text);
	}
	

	public String get(StreamKind streamKind, int streamNumber, int parameterIndex, InfoKind infoKind) {
		return MediaInfoLibrary.INSTANCE.GetI(handle, streamKind.ordinal(), streamNumber, parameterIndex, infoKind.ordinal()).toString();
	}
	

	public int streamCount(StreamKind streamKind) {
		return MediaInfoLibrary.INSTANCE.Count_Get(handle, streamKind.ordinal(), -1);
	}
	

	public int parameterCount(StreamKind streamKind, int streamNumber) {
		return MediaInfoLibrary.INSTANCE.Count_Get(handle, streamKind.ordinal(), streamNumber);
	}
	

	public Map<StreamKind, List<SortedMap<String, String>>> snapshot() {
		Map<StreamKind, List<SortedMap<String, String>>> mediaInfo = new EnumMap<StreamKind, List<SortedMap<String, String>>>(StreamKind.class);
		
		for (StreamKind streamKind : StreamKind.values()) {
			int streamCount = streamCount(streamKind);
			
			if (streamCount > 0) {
				List<SortedMap<String, String>> streamInfoList = new ArrayList<SortedMap<String, String>>(streamCount);
				
				for (int i = 0; i < streamCount; i++) {
					streamInfoList.add(snapshot(streamKind, i));
				}
				
				mediaInfo.put(streamKind, streamInfoList);
			}
		}
		
		return mediaInfo;
	}
	

	public SortedMap<String, String> snapshot(StreamKind streamKind, int streamNumber) {
		TreeMap<String, String> streamInfo = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		
		for (int i = 0, count = parameterCount(streamKind, streamNumber); i < count; i++) {
			String value = get(streamKind, streamNumber, i, InfoKind.Text);
			
			if (value.length() > 0) {
				streamInfo.put(get(streamKind, streamNumber, i, InfoKind.Name), value);
			}
		}
		
		return streamInfo;
	}
	

	@Override
	public void close() {
		MediaInfoLibrary.INSTANCE.Close(handle);
	}
	

	public void dispose() {
		if (handle == null)
			throw new IllegalStateException();
		
		MediaInfoLibrary.INSTANCE.Delete(handle);
		handle = null;
	}
	

	@Override
	protected void finalize() throws Throwable {
		if (handle != null) {
			dispose();
		}
	}
	
	
	public enum StreamKind {
		General,
		Video,
		Audio,
		Text,
		Chapters,
		Image,
		Menu;
	}
	

	public enum InfoKind {
		/**
		 * Unique name of parameter.
		 */
		Name,
		
		/**
		 * Value of parameter.
		 */
		Text,
		
		/**
		 * Unique name of measure unit of parameter.
		 */
		Measure,
		
		Options,
		
		/**
		 * Translated name of parameter.
		 */
		Name_Text,
		
		/**
		 * Translated name of measure unit.
		 */
		Measure_Text,
		
		/**
		 * More information about the parameter.
		 */
		Info,
		
		/**
		 * How this parameter is supported, could be N (No), B (Beta), R (Read only), W
		 * (Read/Write).
		 */
		HowTo,
		
		/**
		 * Domain of this piece of information.
		 */
		Domain;
	}
	
	
	public static String version() {
		return staticOption("Info_Version");
	}
	

	public static String parameters() {
		return staticOption("Info_Parameters");
	}
	

	public static String codecs() {
		return staticOption("Info_Codecs");
	}
	

	public static String capacities() {
		return staticOption("Info_Capacities");
	}
	

	public static String staticOption(String option) {
		return staticOption(option, "");
	}
	

	public static String staticOption(String option, String value) {
		return MediaInfoLibrary.INSTANCE.Option(null, new WString(option), new WString(value)).toString();
	}
	
}
