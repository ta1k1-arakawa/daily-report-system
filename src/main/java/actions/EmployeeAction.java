package actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import constants.PropertyConst;
import services.EmployeeService;

//従業員に関わる処理を行う
public class EmployeeAction extends ActionBase {
    private EmployeeService service;

    @Override
    public void process() throws ServletException, IOException {
        service = new EmployeeService();

        invoke();

        service.close();
    }

    //一覧画面を表示する
    public void index() throws ServletException, IOException {
        //指定されたページ数の一覧画面に表示するデータを取得する
        int page = getPage();
        List<EmployeeView> employees = service.getPerPage(page);

        //すべての従業員データの件数を取得する
        long employeeCount = service.countAll();

        //取得した従業員データ
        putRequestScope(AttributeConst.EMPLOYEES, employees);
        //全ての従業員データの件数
        putRequestScope(AttributeConst.EMP_COUNT, employeeCount);
        //ページ数
        putRequestScope(AttributeConst.PAGE, page);
        //1ページに表示するレコードの数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE);

        //セッションにフラッシュメッセージが設定されているときリクエストスコープに移し替え、セッションから削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if(flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }
        //一覧画面を表示
        forward(ForwardConst.FW_EMP_INDEX);
    }

    //新規登録画面を表示する
    public void entryNew() throws ServletException, IOException {
        //CSRF対策用トークン
        putRequestScope(AttributeConst.TOKEN, getTokenId());
        //空の従業員インスタンス
        putRequestScope(AttributeConst.EMPLOYEE, new EmployeeView());

        //新規登録画面を表示
        forward(ForwardConst.FW_EMP_NEW);
    }

    //新規登録を行う
    public void create() throws ServletException, IOException {
        //CSRF対策tokenのチェック
        if(checkToken()) {
            //パラメータの値を元に従業員情報のインスタンスを作成
            EmployeeView ev = new EmployeeView(
                    null,
                    getRequestParam(AttributeConst.EMP_CODE),
                    getRequestParam(AttributeConst.EMP_NAME),
                    getRequestParam(AttributeConst.EMP_PASS),
                    toNumber(getRequestParam(AttributeConst.EMP_ADMIN_FLG)),
                    null,
                    null,
                    AttributeConst.DEL_FLAG_FALSE.getIntegerValue());

            //アプリケーションスコープからpepper文字列を取得する
            String pepper = getContextScope(PropertyConst.PEPPER);

            //従業員情報登録
            List<String> errors = service.create(ev, pepper);

            if(errors.size() > 0) {
                //登録中にエラーがあった場合

                //CSRF対策用トークン
                putRequestScope(AttributeConst.TOKEN, getTokenId());
                //入力された従業員情報
                putRequestScope(AttributeConst.EMPLOYEE, ev);
                //エラーのリスト
                putRequestScope(AttributeConst.ERR, errors);

                //新規登録画面を再表示
                forward(ForwardConst.FW_EMP_NEW);
            } else {
                //登録中にエラーがなかった場合

                //セッションに登録完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_REGISTERED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
            }
        }
    }

    //詳細画面を表示する
    public void show() throws ServletException, IOException {
        //idを条件に従業員データを取得する
        EmployeeView ev = service.findOne(toNumber(getRequestParam(AttributeConst.EMP_ID)));
        if(ev == null || ev.getDeleteFlag() == AttributeConst.DEL_FLAG_TRUE.getIntegerValue()) {
            //データが取得できなかった、または論理削除されている場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);
            return;
        }

        //取得した従業員情報
        putRequestScope(AttributeConst.EMPLOYEE, ev);

        //詳細画面を表示
        forward(ForwardConst.FW_EMP_SHOW);
    }

    //編集画面を表示する
    public void edit() throws ServletException, IOException {
        //idを条件に従業員データを取得する
        EmployeeView ev = service.findOne(toNumber(getRequestParam(AttributeConst.EMP_ID)));
        if(ev == null || ev.getDeleteFlag() == AttributeConst.DEL_FLAG_TRUE.getIntegerValue()) {
            //データが取得できなかった、または論理削除されている場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);
            return;
        }

        putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
        putRequestScope(AttributeConst.EMPLOYEE, ev); //取得した従業員情報

        //編集画面を表示する
        forward(ForwardConst.FW_EMP_EDIT);
    }

    //更新を行う
    public void update() throws ServletException, IOException {
        //CSRF対策
        if (checkToken()) {
            //パラメータの値を元に従業員情報のインスタンスを作成する
            EmployeeView ev = new EmployeeView(
                    toNumber(getRequestParam(AttributeConst.EMP_ID)),
                    getRequestParam(AttributeConst.EMP_CODE),
                    getRequestParam(AttributeConst.EMP_NAME),
                    getRequestParam(AttributeConst.EMP_PASS),
                    toNumber(getRequestParam(AttributeConst.EMP_ADMIN_FLG)),
                    null,
                    null,
                    AttributeConst.DEL_FLAG_FALSE.getIntegerValue());

            //アプリケーションスコープからpepper文字列を取得
            String pepper = getContextScope(PropertyConst.PEPPER);

            //従業員情報更新
            List<String> errors = service.update(ev, pepper);

            if (errors.size() > 0) {
                //更新中にエラーが発生した場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                putRequestScope(AttributeConst.EMPLOYEE, ev); //入力された従業員情報
                putRequestScope(AttributeConst.ERR, errors); //エラーのリスト

                //編集画面を再表示
                forward(ForwardConst.FW_EMP_EDIT);
            } else {
                //更新中にエラーがなかった場合

                //セッションに更新完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_UPDATED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
            }
        }
    }

    //論理削除を行う
    public void destroy() throws ServletException, IOException {

        //CSRF対策
        if (checkToken()) {

            //idを条件に従業員データを論理削除する
            service.destroy(toNumber(getRequestParam(AttributeConst.EMP_ID)));

            //セッションに削除完了のフラッシュメッセージを設定
            putSessionScope(AttributeConst.FLUSH, MessageConst.I_DELETED.getMessage());

            //一覧画面にリダイレクト
            redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
        }
    }
}

















