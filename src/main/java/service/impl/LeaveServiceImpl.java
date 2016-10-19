package service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mapper.LeaveApplyMapper;
import po.LeaveApply;
import service.LeaveService;
@Service
public class LeaveServiceImpl implements LeaveService{
	@Autowired
	LeaveApplyMapper leavemapper;
	@Autowired
	IdentityService identityservice;
	@Autowired
	RuntimeService runtimeservice;
	@Autowired
	TaskService taskservice;
	
	public ProcessInstance startWorkflow(LeaveApply apply, String userid, Map<String, Object> variables) {
		apply.setApply_time(new Date().toString());
		apply.setUser_id(userid);
		leavemapper.save(apply);
		String businesskey=String.valueOf(apply.getId());//使用leaveapply表的主键作为businesskey,连接业务数据和流程数据
		identityservice.setAuthenticatedUserId(userid);
		ProcessInstance instance=runtimeservice.startProcessInstanceByKey("leave",businesskey,variables);
		System.out.println(businesskey);
		String instanceid=instance.getId();
		apply.setProcess_instance_id(instanceid);
		leavemapper.update(apply);
		return instance;
	}

	public List<LeaveApply> getpagedepttask(String userid,int firstrow,int rowcount) {
		List<LeaveApply> results=new ArrayList<LeaveApply>();
		List<Task> tasks=taskservice.createTaskQuery().taskCandidateGroup("部门经理").listPage(firstrow, rowcount);
		for(Task task:tasks){
			String instanceid=task.getProcessInstanceId();
			ProcessInstance ins=runtimeservice.createProcessInstanceQuery().processInstanceId(instanceid).singleResult();
			String businesskey=ins.getBusinessKey();
			LeaveApply a=leavemapper.get(Integer.parseInt(businesskey));
			a.setTask(task);
			results.add(a);
		}
		return results;
	}
	
	public int getalldepttask(String userid) {
		List<Task> tasks=taskservice.createTaskQuery().taskCandidateGroup("部门经理").list();
		return tasks.size();
	}

	public LeaveApply getleave(int id) {
		LeaveApply leave=leavemapper.get(id);
		return leave;
	}

	public List<LeaveApply> getpagehrtask(String userid,int firstrow,int rowcount) {
		List<LeaveApply> results=new ArrayList<LeaveApply>();
		List<Task> tasks=taskservice.createTaskQuery().taskCandidateGroup("人事").listPage(firstrow, rowcount);
		for(Task task:tasks){
			String instanceid=task.getProcessInstanceId();
			ProcessInstance ins=runtimeservice.createProcessInstanceQuery().processInstanceId(instanceid).singleResult();
			String businesskey=ins.getBusinessKey();
			LeaveApply a=leavemapper.get(Integer.parseInt(businesskey));
			a.setTask(task);
			results.add(a);
		}
		return results;
	}

	public int getallhrtask(String userid) {
		List<Task> tasks=taskservice.createTaskQuery().taskCandidateGroup("人事").list();
		return tasks.size();
	}
	
	public List<LeaveApply> getpageXJtask(String userid,int firstrow,int rowcount) {
		List<LeaveApply> results=new ArrayList<LeaveApply>();
		List<Task> tasks=taskservice.createTaskQuery().taskCandidateOrAssigned(userid).taskName("销假").listPage(firstrow, rowcount);
		for(Task task:tasks){
			String instanceid=task.getProcessInstanceId();
			ProcessInstance ins=runtimeservice.createProcessInstanceQuery().processInstanceId(instanceid).singleResult();
			String businesskey=ins.getBusinessKey();
			LeaveApply a=leavemapper.get(Integer.parseInt(businesskey));
			a.setTask(task);
			results.add(a);
		}
		return results;
	}

	public int getallXJtask(String userid) {
		List<Task> tasks=taskservice.createTaskQuery().taskCandidateOrAssigned(userid).taskName("销假").list();
		return tasks.size();
	}
	
	public List<LeaveApply> getpageupdateapplytask(String userid,int firstrow,int rowcount) {
		List<LeaveApply> results=new ArrayList<LeaveApply>();
		List<Task> tasks=taskservice.createTaskQuery().taskCandidateOrAssigned(userid).taskName("调整申请").listPage(firstrow, rowcount);
		for(Task task:tasks){
			String instanceid=task.getProcessInstanceId();
			ProcessInstance ins=runtimeservice.createProcessInstanceQuery().processInstanceId(instanceid).singleResult();
			String businesskey=ins.getBusinessKey();
			LeaveApply a=leavemapper.get(Integer.parseInt(businesskey));
			a.setTask(task);
			results.add(a);
		}
		return results;
	}
	
	public int getallupdateapplytask(String userid) {
		List<Task> tasks=taskservice.createTaskQuery().taskCandidateOrAssigned(userid).taskName("调整申请").list();
		return tasks.size();
	}
	
	public void completereportback(String taskid, String realstart_time, String realend_time) {
		Task task=taskservice.createTaskQuery().taskId(taskid).singleResult();
		String instanceid=task.getProcessInstanceId();
		ProcessInstance ins=runtimeservice.createProcessInstanceQuery().processInstanceId(instanceid).singleResult();
		String businesskey=ins.getBusinessKey();
		LeaveApply a=leavemapper.get(Integer.parseInt(businesskey));
		a.setReality_start_time(realstart_time);
		a.setReality_end_time(realend_time);
		leavemapper.update(a);
		taskservice.complete(taskid);
	}

	public void updatecomplete(String taskid, LeaveApply leave,String reapply) {
		Task task=taskservice.createTaskQuery().taskId(taskid).singleResult();
		String instanceid=task.getProcessInstanceId();
		ProcessInstance ins=runtimeservice.createProcessInstanceQuery().processInstanceId(instanceid).singleResult();
		String businesskey=ins.getBusinessKey();
		LeaveApply a=leavemapper.get(Integer.parseInt(businesskey));
		a.setLeave_type(leave.getLeave_type());
		a.setStart_time(leave.getStart_time());
		a.setEnd_time(leave.getEnd_time());
		a.setReason(leave.getReason());
		Map<String,Object> variables=new HashMap<String,Object>();
		variables.put("reapply", reapply);
		if(reapply.equals("true")){
			leavemapper.update(a);
			taskservice.complete(taskid,variables);
		}else
			taskservice.complete(taskid,variables);
	}

}
