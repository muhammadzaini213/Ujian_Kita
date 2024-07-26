package com.muhammadzaini.ujiankita.main;


public class ExamListData {

    private String logoUrl, name, grade, date, time, duration, excelUrl, isActive, enterCode, exitCode;

    public ExamListData(String logoUrl, String name, String grade, String date, String time, String duration, String excelUrl, String isActive, String enterCode, String exitCode) {
        this.logoUrl = logoUrl;
        this.name = name;
        this.grade = grade;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.excelUrl = excelUrl;
        this.isActive = isActive;
        this.enterCode = enterCode;
        this.exitCode = exitCode;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getName() {
        return name;
    }

    public String getGrade() {
        return grade;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDuration() {
        return duration;
    }

    public String getExcelUrl() {
        return excelUrl;
    }

    public String getIsActive() {
        return isActive;
    }

    public String getEnterCode() {
        return enterCode;
    }

    public String getExitCode() {
        return exitCode;
    }
}


