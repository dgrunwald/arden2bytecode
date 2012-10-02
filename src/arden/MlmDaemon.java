package arden;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.events.EvokeEvent;

public class MlmDaemon implements Runnable {
	List<MedicalLogicModule> mlms;	
	ExecutionContext context;
	ArdenValue[] arguments;
	
	public MlmDaemon(List<MedicalLogicModule> mlms, ExecutionContext context, ArdenValue[] arguments) {
		this.mlms = mlms;
		this.context = context;
		this.arguments = arguments;		
	}
	
	private SortedMap<ArdenTime, List<MedicalLogicModule>> createSchedule(List<MedicalLogicModule> mlms) {
		SortedMap<ArdenTime, List<MedicalLogicModule>> mlmSchedule = new TreeMap<ArdenTime, List<MedicalLogicModule>>(new ArdenTime.NaturalComparator());
		for (MedicalLogicModule mlm : mlms) {
			try {
				EvokeEvent e = mlm.getEvoke(context, arguments);
				ArdenTime currentTime = context.getCurrentTime();
				ArdenTime nextRuntime = e.getNextRunTime(context);
				if (nextRuntime == null || currentTime.compareTo(nextRuntime) > 0) {
					continue;
				}
				List<MedicalLogicModule> alreadyScheduled = mlmSchedule.get(nextRuntime);
				if (alreadyScheduled == null) {
					List<MedicalLogicModule> toSchedule = new LinkedList<MedicalLogicModule>();
					toSchedule.add(mlm);
					mlmSchedule.put(nextRuntime, toSchedule);
				} else {
					alreadyScheduled.add(mlm);
				}
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		return mlmSchedule;
	}
	
	@Override
	public void run() {
		while (true) {
			SortedMap<ArdenTime, List<MedicalLogicModule>> mlmSchedule = createSchedule(mlms);
			if (mlmSchedule.isEmpty()) {
				break;
			}
			ArdenTime nextRuntime = mlmSchedule.firstKey();
			List<MedicalLogicModule> scheduledMlms = mlmSchedule.get(nextRuntime);
			ArdenTime currentTime = context.getCurrentTime();
			long delay = nextRuntime.value - currentTime.value;
			if (delay >= 0) {
				try {
					Thread.sleep(nextRuntime.value - currentTime.value);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				for (MedicalLogicModule mlm : scheduledMlms) {
					try {
						mlm.run(context, arguments);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			}
			while (nextRuntime.compareTo(context.getCurrentTime()) >= 0) {
				try {
					Thread.sleep(1); // make sure at least 1 ms passes
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
