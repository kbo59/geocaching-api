package com.arcao.geocaching.api.parser;


import com.arcao.geocaching.api.data.type.AttributeType;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.data.type.GeocacheType;
import com.arcao.geocaching.api.data.type.MemberType;
import com.arcao.geocaching.api.data.type.TrackableLogType;
import com.google.gson.stream.JsonToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonParserUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonParserUtil.class);
    private static final long HOUR_IN_MS = 1000 * 60 * 60;

    private JsonParserUtil() {
    }

    static Date parseJsonDate(String date) {
        Pattern DATE_PATTERN = Pattern.compile("/Date\\((-?\\d+)([-+]\\d{4})?\\)/");

        if (date == null)
            return null;

        Matcher m = DATE_PATTERN.matcher(date);
        if (m.matches()) {
            long time = Long.parseLong(m.group(1));
            long zone = 0;
            if (m.group(2) != null && !m.group(2).isEmpty())
                zone = Integer.parseInt(m.group(2)) / 100 * HOUR_IN_MS;
            return new Date(time + zone);
        }

        logger.error("parseJsonDate failed: " + date);
        return null;
    }

    static Date parseJsonUTCDate(String date) {
        Pattern DATE_PATTERN = Pattern.compile("/Date\\((-?\\d+)([-+]\\d{4})?\\)/");

        if (date == null)
            return null;

        Matcher m = DATE_PATTERN.matcher(date);
        if (m.matches()) {
            long time = Long.parseLong(m.group(1));
            // zone is always zero for UTC
            long zone = 0;
            return new Date(time + zone);
        }

        logger.error("parseJsonDate failed: " + date);
        return null;
    }

    static GeocacheType parseGeocacheType(JsonReader r) throws IOException {
        GeocacheType geocacheType = null;

        if (isNextNull(r))
            return null;

        r.beginObject();
        while (r.hasNext()) {
            String name = r.nextName();
            if ("GeocacheTypeId".equals(name)) {
                geocacheType = GeocacheType.fromId(r.nextInt());
            } else {
                r.skipValue();
            }
        }
        r.endObject();
        return geocacheType;
    }

    static ContainerType parseContainerType(JsonReader r) throws IOException {
        ContainerType containerType = ContainerType.NotChosen;

        if (isNextNull(r))
            return containerType;

        r.beginObject();
        while (r.hasNext()) {
            String name = r.nextName();
            if ("ContainerTypeId".equals(name)) {
                containerType = ContainerType.fromId(r.nextInt());
            } else {
                r.skipValue();
            }
        }
        r.endObject();
        return containerType;
    }

    static MemberType parseMemberType(JsonReader r) throws IOException {
        MemberType memberType = null;

        if (isNextNull(r))
            return null;

        if (r.peek() == JsonToken.NUMBER) {
            memberType = MemberType.fromId(r.nextInt() / 10);
            return memberType;
        }

        r.beginObject();
        while (r.hasNext()) {
            String name = r.nextName();
            if ("MemberTypeId".equals(name)) {
                memberType = MemberType.fromId(r.nextInt());
            } else {
                r.skipValue();
            }
        }
        r.endObject();
        return memberType;
    }

    private static AttributeType parseAttribute(JsonReader r) throws IOException {
        int id = 1;
        boolean on = false;

        r.beginObject();
        while (r.hasNext()) {
            String name = r.nextName();
            if ("AttributeTypeID".equals(name)) {
                id = r.nextInt();
            } else if ("IsOn".equals(name)) {
                on = r.nextBoolean();
            } else {
                r.skipValue();
            }
        }
        r.endObject();

        return AttributeType.fromId(id, on);
    }

    static EnumSet<AttributeType> parseAttributeList(JsonReader r) throws IOException {
        if (r.peek() != JsonToken.BEGIN_ARRAY) {
            r.skipValue();
        }

        EnumSet<AttributeType> attributeSet = EnumSet.noneOf(AttributeType.class);
        r.beginArray();
        while (r.hasNext()) {
            AttributeType attribute = parseAttribute(r);
            if (attribute != null) {
                attributeSet.add(attribute);
            }
        }
        r.endArray();
        return attributeSet;
    }

    static TrackableLogType parseTrackableLogType(JsonReader r) throws IOException {
        TrackableLogType trackableLogType = null;

        if (isNextNull(r))
            return null;

        r.beginObject();
        while (r.hasNext()) {
            String name = r.nextName();
            if ("WptLogTypeId".equals(name)) {
                trackableLogType = TrackableLogType.fromId(r.nextInt());
            } else {
                r.skipValue();
            }
        }
        r.endObject();
        return trackableLogType;
    }

    static boolean isNextNull(JsonReader r) throws IOException {
        if (r.peek() == JsonToken.NULL) {
            r.nextNull();
            return true;
        }

        return false;
    }
}
