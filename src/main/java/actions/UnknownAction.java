package actions;

import java.io.IOException;

import javax.servlet.ServletException;

import constants.ForwardConst;

//エラー発生時の処理を行う
public class UnknownAction extends ActionBase {
    //共通エラー画面を表示する
    @Override
    public void process() throws ServletException, IOException {
        forward(ForwardConst.FW_ERR_UNKNOWN);
    }
}
