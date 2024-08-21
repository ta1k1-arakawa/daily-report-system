package models.validators;

import java.util.ArrayList;
import java.util.List;

import actions.views.ReportView;
import constants.MessageConst;

//日報インスタンスに設定されている値のバリデーションを行うクラス
public class ReportValidator {
    //日報インスタンスの各項目についてバリデーションを行う
    public static List<String> validate(ReportView rv) {
        List<String> errors = new ArrayList<String>();

        //タイトルのチェック
        String titleError = validateTitle(rv.getTitle());
        if(!titleError.equals("")) {
            errors.add(titleError);
        }

        //内容のチェック
        String contentError = validateContent(rv.getContent());
        if(!contentError.endsWith("")) {
            errors.add(contentError);
        }

        return errors;
    }

    //タイトルに入力値があるかチェックし、なければエラーメッセージを返す
    private static String validateTitle(String title) {
        if(title == null || title.equals("")) {
            return MessageConst.E_NOTITLE.getMessage();
        }

        //入力値があるときは空文字を返す
        return "";
    }

    //入力値に値があるかチェックし、なければエラーメッセージを返す
    private static String validateContent(String content) {
        if(content == null || content.equals("")) {
            return MessageConst.E_NOTITLE.getMessage();
        }

        //入力値があるときは空文字を返す
        return "";
    }
}



















