package com.baidu.track.data;

/**
 * 任务单实体类
 */
public class Task {
    private String tNo;//任务工单编码
    private String taskName;//任务标题
    private String content;//任务内容
    private String time;//任务时间
    private String lastTime;//任务办结时限
    private String address;//任务地址
    //'latitude', 'longitude',纬度经度
    private String latitude;//定位纬度
    private String longitude;//定位经度
    private String imgUrl;//图片地址
    private String getimgUrl;//12345传图片地址

    private String state;//任务状态



    public Task(){

    }
    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String gettNo() {
        return tNo;
    }

    public void settNo(String tNo) {
        this.tNo = tNo;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getGetimgUrl() {
        return getimgUrl;
    }

    public void setGetimgUrl(String getimgUrl) {
        this.getimgUrl = getimgUrl;
    }


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
