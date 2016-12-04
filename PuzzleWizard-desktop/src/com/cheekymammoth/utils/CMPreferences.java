package com.cheekymammoth.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;


public class CMPreferences implements Preferences {
	//private final String name;
    private final Properties properties = new Properties();
    private final FileHandle file;

    public CMPreferences (String name) {
            this(new LwjglFileHandle(new File(".prefs/" + name), FileType.External));
    }

    public CMPreferences (FileHandle file) {
            //this.name = file.name();
            this.file = file;
            if (!file.exists()) return;

            InputStream in = null;
            try {
                    in = new BufferedInputStream(file.read());
                    properties.loadFromXML(in);
            } catch (Throwable t) {
                    t.printStackTrace();
            } finally {
                    StreamUtils.closeQuietly(in);
            }
    }
    
    public String getFilePath() {
    	return file != null && file.file() != null ? file.file().getAbsolutePath() : null;
    }

    @Override
    public void putBoolean (String key, boolean val) {
            properties.put(key, Boolean.toString(val));
    }

    @Override
    public void putInteger (String key, int val) {
            properties.put(key, Integer.toString(val));
    }

    @Override
    public void putLong (String key, long val) {
            properties.put(key, Long.toString(val));
    }

    @Override
    public void putFloat (String key, float val) {
            properties.put(key, Float.toString(val));
    }

    @Override
    public void putString (String key, String val) {
            properties.put(key, val);
    }

    @Override
    public void put (Map<String, ?> vals) {
            for (Entry<String, ?> val : vals.entrySet()) {
                    if (val.getValue() instanceof Boolean) putBoolean(val.getKey(), (Boolean)val.getValue());
                    if (val.getValue() instanceof Integer) putInteger(val.getKey(), (Integer)val.getValue());
                    if (val.getValue() instanceof Long) putLong(val.getKey(), (Long)val.getValue());
                    if (val.getValue() instanceof String) putString(val.getKey(), (String)val.getValue());
                    if (val.getValue() instanceof Float) putFloat(val.getKey(), (Float)val.getValue());
            }
    }

    @Override
    public boolean getBoolean (String key) {
            return getBoolean(key, false);
    }

    @Override
    public int getInteger (String key) {
            return getInteger(key, 0);
    }

    @Override
    public long getLong (String key) {
            return getLong(key, 0);
    }

    @Override
    public float getFloat (String key) {
            return getFloat(key, 0);
    }

    @Override
    public String getString (String key) {
            return getString(key, "");
    }

    @Override
    public boolean getBoolean (String key, boolean defValue) {
    	boolean b = false;
    	try {
            b = Boolean.parseBoolean(properties.getProperty(key, Boolean.toString(defValue)));
    	} catch (NumberFormatException e) { }
    	
    	return b;
    }

    @Override
    public int getInteger (String key, int defValue) {
    	int i = Integer.MIN_VALUE;
    	try {
            i = Integer.parseInt(properties.getProperty(key, Integer.toString(defValue)));
    	} catch (NumberFormatException e) { }
    	
    	return i;
    }

    @Override
    public long getLong (String key, long defValue) {
    	long l = Long.MIN_VALUE;
    	try {
            l = Long.parseLong(properties.getProperty(key, Long.toString(defValue)));
    	} catch (NumberFormatException e) { }
    	
    	return l;
    }

    @Override
    public float getFloat (String key, float defValue) {
    	float f = Float.MIN_VALUE;
    	try {
            return Float.parseFloat(properties.getProperty(key, Float.toString(defValue)));
    	} catch (NumberFormatException e) { }
    	
    	return f;
    }

    @Override
    public String getString (String key, String defValue) {
            return properties.getProperty(key, defValue);
    }

    @Override
    public Map<String, ?> get () {
    	try {
            Map<String, Object> map = new HashMap<String, Object>();
            for (Entry<Object, Object> val : properties.entrySet()) {
                    if (val.getValue() instanceof Boolean)
                            map.put((String)val.getKey(), (Boolean)Boolean.parseBoolean((String)val.getValue()));
                    if (val.getValue() instanceof Integer) map.put((String)val.getKey(), (Integer)Integer.parseInt((String)val.getValue()));
                    if (val.getValue() instanceof Long) map.put((String)val.getKey(), (Long)Long.parseLong((String)val.getValue()));
                    if (val.getValue() instanceof String) map.put((String)val.getKey(), (String)val.getValue());
                    if (val.getValue() instanceof Float) map.put((String)val.getKey(), (Float)Float.parseFloat((String)val.getValue()));
            }
            return map;
    	} catch (Exception e) { return null; }
    }

    @Override
    public boolean contains (String key) {
            return properties.containsKey(key);
    }

    @Override
    public void clear () {
            properties.clear();
    }

    @Override
    public void flush () {
            OutputStream out = null;
            try {
                    out = new BufferedOutputStream(file.write(false));
                    properties.storeToXML(out, null);
            } catch (Exception ex) {
                    throw new GdxRuntimeException("Error writing preferences: " + file, ex);
            } finally {
                    StreamUtils.closeQuietly(out);
            }
    }

    @Override
    public void remove (String key) {
            properties.remove(key);
    }
}
