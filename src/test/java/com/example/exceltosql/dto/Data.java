package com.example.exceltosql.dto;

/**
 * @author :sunjian23
 * @date : 2023/12/11 18:21
 */
public class Data {

    private long taskId;
    private String taskResult;
    private String filePath;
    private String status;
    private boolean confirmed;
    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
    public long getTaskId() {
        return taskId;
    }

    public void setTaskResult(String taskResult) {
        this.taskResult = taskResult;
    }
    public String getTaskResult() {
        return taskResult;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getFilePath() {
        return filePath;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
    public boolean getConfirmed() {
        return confirmed;
    }

}