package lab2;

import java.util.Comparator;

public class TimeStampedMessageComparator implements Comparator<TimeStampedMessage>{

	@Override
	public int compare(TimeStampedMessage m1, TimeStampedMessage m2) {
		Clock clock1 = m1.getTimeStamp();
		Clock clock2 = m2.getTimeStamp();
		int result = clock1.compareClock(clock2);
		if(result == 0){
			m1.setConcurrent();
			m2.setConcurrent();
		}
		return result;
				
	}
	
}
