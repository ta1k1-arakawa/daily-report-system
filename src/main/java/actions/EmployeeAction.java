package actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
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
    ;}
}