package vn.vnsky.bcss.admin.service;

import vn.vnsky.bcss.admin.dto.MailInfoDTO;


public interface MailService {

    void sendTokenActivateAccount(MailInfoDTO dto);

    void sendForgotPassword(MailInfoDTO dto);

}
