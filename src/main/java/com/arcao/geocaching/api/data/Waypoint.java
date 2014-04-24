package com.arcao.geocaching.api.data;


import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;

import com.arcao.geocaching.api.data.coordinates.Coordinates;
import com.arcao.geocaching.api.data.type.WaypointType;

public class Waypoint implements Serializable {
	private static final long serialVersionUID = -7183357014183017947L;

	private final Coordinates coordinates;
	private final Date time;
	private final String waypointCode;
	private final String name;
	private final String note;
	private final WaypointType waypointType;
	private final String iconName;

	public Waypoint(Coordinates coordinates, Date time, String waypointCode, String name, String note, WaypointType waypointType) {
		this.coordinates = coordinates;
		this.time = time;
		this.waypointCode = waypointCode;
		this.name = name;
		this.note = note;
		this.waypointType = waypointType;
		this.iconName = waypointType.getIconName();
	}
	
	public Coordinates getCoordinates() {
    return coordinates;
  }

	public double getLatitude() {
		return coordinates.getLatitude();
	}

	public double getLongitude() {
		return coordinates.getLongitude();
	}

	public Date getTime() {
		return time;
	}

	public String getWaypointCode() {
		return waypointCode;
	}

	public String getName() {
		return name;
	}

	public String getNote() {
		return note;
	}

	public WaypointType getWaypointType() {
		return waypointType;
	}

	public String getIconName() {
		return iconName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Method m : getClass().getMethods()) {
			if ((!m.getName().startsWith("get") && !m.getName().startsWith("is")) ||
					m.getParameterTypes().length != 0 ||
					void.class.equals(m.getReturnType()))
				continue;

			sb.append(m.getName());
			sb.append(':');
			try {
				sb.append(m.invoke(this, new Object[0]));
			} catch (Exception e) {
			}
			sb.append(", ");
		}
		return sb.toString();
	}
}
