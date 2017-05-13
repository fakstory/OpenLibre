package de.dorianscholz.openlibre.model;

import java.util.Locale;
import java.util.TimeZone;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import static java.lang.Math.max;

public class RawTagData extends RealmObject {
    public static final String ID = "id";
    public static final String DATE = "date";
    public static final String TIMEZONE_OFFSET_IN_MINUTES = "timezoneOffsetInMinutes";
    static final String SENSOR = "sensor";
    static final String DATA = "data";

    private static final int offsetTrendTable = 28;
    private static final int offsetHistoryTable = 124;
    private static final int offsetTrendIndex = 26;
    private static final int offsetHistoryIndex = 27;
    private static final int offsetSensorAge = 316;
    private static final int tableEntrySize = 6;
    private static final int sensorInitializationInMinutes = 60;

    @PrimaryKey
    String id;
    public long date = -1;
    public int timezoneOffsetInMinutes;
    SensorData sensor;
    byte[] data;

    public RawTagData() {}

    public RawTagData(SensorData sensorData, byte[] data) {
        date = System.currentTimeMillis();
        timezoneOffsetInMinutes = TimeZone.getDefault().getOffset(date) / 1000 / 60;
        sensor = sensorData;
        id = String.format(Locale.US, "%s_%d", sensor.id, date);
        this.data = data.clone();
    }

    int getTrendValue(int index) {
        return getWord(offsetTrendTable + index * tableEntrySize) & 0x3FFF;
    }

    int getHistoryValue(int index) {
        return getWord(offsetHistoryTable + index * tableEntrySize) & 0x3FFF;
    }

    private static int makeWord(byte high, byte low) {
        return 0x100 * (high & 0xFF) + (low & 0xFF);
    }

    private int getWord(int offset) {
        return getWord(data, offset);
    }

    private static int getWord(byte[] data, int offset) {
        return makeWord(data[offset + 1], data[offset]);
    }

    private int getByte(int offset) {
        return data[offset] & 0xFF;
    }

    int getIndexTrend() {
        return getByte(offsetTrendIndex);
    }

    int getIndexHistory() {
        return getByte(offsetHistoryIndex);
    }

    int getSensorAgeInMinutes() {
        return getSensorAgeInMinutes(data);
    }

    private static int getSensorAgeInMinutes(byte[] data) {
        return getWord(data, offsetSensorAge);
    }

    public static int getSensorReadyInMinutes(byte[] data) {
        return max(0, sensorInitializationInMinutes - getSensorAgeInMinutes(data));
    }
}
