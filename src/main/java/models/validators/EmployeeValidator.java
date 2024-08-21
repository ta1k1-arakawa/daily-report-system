package models.validators;

import java.util.ArrayList;
import java.util.List;

import actions.views.EmployeeView;
import constants.MessageConst;
import services.EmployeeService;

//従業員インスタンスに設定されているバリデーションを行うクラス
public class EmployeeValidator {
    //従業員インスタンスの各項目についてバリデーションを行う
    public static List<String> validate(
            EmployeeService service,
            EmployeeView ev,
            Boolean codeDuplicateCheckFlag,
            Boolean passwordCheckFlag) {
        List<String> errors = new ArrayList<String>();

        //社員番号のチェック
        String codeError = validateCode(service, ev.getCode(), codeDuplicateCheckFlag);
        if(!codeError.equals("")){
            errors.add(codeError);
        }

        //氏名のチェック
        String nameError = validateName(ev.getName());
        if(!nameError.equals("")) {
            errors.add(nameError);
        }

        String passError = validatePassword(ev.getPassword(), passwordCheckFlag);
        if(!passError.equals("")) {
            errors.add(passError);
        }

        return errors;
    }

    //社員番号のチェックを行い、エラーメッセージを返す
    private static String validateCode(EmployeeService service, String code, Boolean codeDuplicateCheckFlag) {
        //入力値がなければエラーメッセージを返す
        if(code == null || code.equals("")) {
            return MessageConst.E_NOEMP_CODE.getMessage();
        }

        if(codeDuplicateCheckFlag) {
            //社員番号の重複チェック
            long employeeCount = isDuplicateEmployee(service, code);

            //同一社員番号が既に登録されている場合はエラーメッセージを返す
            if(employeeCount > 0) {
                return MessageConst.E_EMP_CODE_EXIST.getMessage();
            }
        }

        //エラーがないとき空文字を返す
        return "";
    }

    private static long isDuplicateEmployee(EmployeeService service, String code) {
        long employeeCount = service.countByCode(code);
        return employeeCount;
    }

    //氏名に入力値があるかチェックし、なければエラーメッセージを返す
    private static String validateName(String name) {
        if(name == null || name.equals("")) {
            return MessageConst.E_NONAME.getMessage();
        }

        //入力値があるときは空文字を返す
        return "";
    }

    //パスワードの入力チェックを行い、エラーメッセージを返す
    private static String validatePassword(String password, Boolean passwordCheckFlag) {
        if(passwordCheckFlag && (password == null || password.equals(""))) {
            return MessageConst.E_NOPASSWORD.getMessage();
        }

        //エラーがないときは空文字を返す
        return "";
    }
}
