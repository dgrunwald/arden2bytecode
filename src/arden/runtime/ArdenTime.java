// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package arden.runtime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

public final class ArdenTime extends ArdenValue {
	/** Number of milliseconds since 1.1.1970, midnight GMT */
	public final long value;

	public ArdenTime(long value) {
		this.value = value;
	}

	public ArdenTime(Date value) {
		this.value = value.getTime();
	}

	public ArdenTime(long value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new ArdenTime(value, newPrimaryTime);
	}

	public static final DateFormat isoDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public static final DateFormat isoDateTimeFormatWithMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	public static final DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public String toString() {
		if (value % 1000 != 0) {
			return isoDateTimeFormatWithMillis.format(new Date(value));
		} else {
			return isoDateTimeFormat.format(new Date(value));
		}
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ArdenTime) && (value == ((ArdenTime) obj).value);
	}

	@Override
	public int hashCode() {
		return new Long(value).hashCode();
	}

	@Override
	public int compareTo(ArdenValue rhs) {
		if (rhs instanceof ArdenTime) {
			long rval = ((ArdenTime) rhs).value;
			if (value < rval)
				return -1;
			else if (value > rval)
				return 1;
			else
				return 0;
		}
		return Integer.MIN_VALUE;
	}

	private long addMonths(double months) {
		int wholeMonths = (int) months;
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(value);
		c.add(GregorianCalendar.MONTH, wholeMonths);
		return c.getTimeInMillis() + (long) ((months - wholeMonths) * 1000 * ArdenDuration.SECONDS_PER_MONTH);
	}

	public long add(ArdenDuration dur) {
		if (dur.isMonths) {
			return addMonths(dur.value);
		} else {
			long milliseconds = (long) (1000 * dur.value);
			return this.value + milliseconds;
		}
	}

	public long subtract(ArdenDuration dur) {
		if (dur.isMonths) {
			return addMonths(-dur.value);
		} else {
			long milliseconds = (long) (1000 * dur.value);
			return this.value - milliseconds;
		}
	}
	
	public static class NaturalComparator implements Comparator<ArdenTime> {
		@Override
		public int compare(ArdenTime arg0, ArdenTime arg1) {
			if (arg0 == null) {
				if (arg1 == null) {
					return 0;
				}
				return 1;
			} else if (arg1 == null) {
				return -1;
			}
			return new Long(arg0.value).compareTo(new Long(arg1.value));
		}
	}
}
