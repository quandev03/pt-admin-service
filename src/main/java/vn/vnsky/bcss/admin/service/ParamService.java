package vn.vnsky.bcss.admin.service;

import vn.vnsky.bcss.admin.dto.ParamDTO;

import java.util.List;
import java.util.Map;

public interface ParamService {

    List<ParamDTO> getParamByCode(String code, String language);

    Map<String, List<ParamDTO>> getMultipleParams(String language, String redisKey);

    String getKey(String key, String languageCode);

    List<ParamDTO> getSystemUrls();
}

