package com.example.exceltosql.entity;

import lombok.Data;

/**
 * @author yujiawei
 * @date 2020年1月10日
 */
@Data
public class MultiLanguageTasksReturnModel {

    //@ApiModelProperty(required = true, notes = "任务ID", example = "1035663")
    private Integer taskId;

    //@ApiModelProperty(required = true, notes = "任务结果", example = "生成语言包完成！受控版本不是最新版本，请重新进行合规检测")
    private String taskResult = "";

    //@ApiModelProperty(required = true, notes = "文件地址", example = "documents/multiLanguageManagement/languagePackage/pms_language_1.5.100.20230119183616.zip")
    private String filePath = "";

    //@ApiModelProperty(required = true, notes = "任务状态", example = "成功")
    private String status = "";

    //@ApiModelProperty(required = true, notes = "确认状态", example = "false")
    private Boolean confirmed = false;
}