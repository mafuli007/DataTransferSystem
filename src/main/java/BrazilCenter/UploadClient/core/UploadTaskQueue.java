package BrazilCenter.UploadClient.core;

import java.util.LinkedList;
import java.util.Queue;

import BrazilCenter.UploadClient.task.UploadTask;

/**
 * As a message queue. 
 * the scan thread work as a producer puts tasks into the queue.
 * */
public class UploadTaskQueue {

 	private Queue<UploadTask> tasklist;

	public UploadTaskQueue() {
		this.tasklist = new LinkedList<UploadTask>();
	}
	
	public UploadTask GetTask() {
		synchronized (this) {
			UploadTask task = this.tasklist.poll();
			return task;
		}
	}
	public void AddTask(UploadTask task) {
		synchronized (this) {
			this.tasklist.add(task);
		}
	}
}
