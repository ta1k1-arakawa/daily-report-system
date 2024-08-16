package actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import constants.AttributeConst;
import constants.ForwardConst;
import constants.PropertyConst;

public abstract class ActionBase {
    protected ServletContext context;
    protected HttpServletRequest request;
    protected HttpServletResponse response;

    //初期化処理
    public void init(
            ServletContext servletContext,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        this.context = servletContext;
        this.request = servletRequest;
        this.response = servletResponse;
    }

    //フロントコントローラーが読み出されるメソッド
    public abstract void process() throws ServletException, IOException;

    //パラメータのcommandの値に該当するメソッドを実行
    protected void invoke() throws ServletException, IOException{
        Method commandMethod;
        try {
            //パラメータからcommandを取得
            String command = request.getParameter(ForwardConst.CMD.getValue());

            //commandに該当するメソッドを実行する
            commandMethod = this.getClass().getDeclaredMethod(command, new Class[0]);
            commandMethod.invoke(this, new Object[0]);

        } catch(NoSuchMethodException
                | SecurityException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | NullPointerException e) {

            //例外を表示
            e.printStackTrace();
            //commandの値が防いで実行できない場合エラー画面を呼び出し
            forward(ForwardConst.FW_ERR_UNKNOWN);
        }
    }

    //指定されたjspの呼び出しを行う
    protected void forward(ForwardConst target) throws ServletException, IOException{
        //jspファイルの相対パスを作成
        String forward = String.format("/WEB-INF/views/%s.jsp", target.getValue());
        RequestDispatcher dispatcher = request.getRequestDispatcher(forward);

        //jspファイルの呼び出し
        dispatcher.forward(request, response);
    }

   //URLを構築しリダイレクトを行う
    protected void redirect(ForwardConst action, ForwardConst command) throws ServletException, IOException{
        //URLを構築
        String redirectUrl = request.getContextPath() + "/?action=" + action.getValue();
        if(command != null) {
            redirectUrl = redirectUrl + "&command=" + command.getValue();
        }

        //URLへリダイレクト
        response.sendRedirect(redirectUrl);
    }

    //CSRF対策
    protected boolean checkToken() throws ServletException, IOException {
        //パラメータからtokenの値を取得
        String _token = getRequestParam(AttributeConst.TOKEN);

        if(_token == null || !(_token.equals(getTokenId()))){
            forward(ForwardConst.FW_ERR_UNKNOWN);

            return false;
        } else {
            return true;
        }
    }

    //セッションIDを取得
    protected String getTokenId() {
        return request.getSession().getId();
    }

    //リクエストから表示を要求されているページ数を取得し、返す
    protected int getPage() {
        int page;
        page = toNumber(request.getParameter(AttributeConst.PAGE.getValue()));
        if(page == Integer.MIN_VALUE) {
            page = 1;
        }
        return page;
    }

    //文字列を数値に変換する
    protected int toNumber(String strNumber) {
        int number = 0;
        try {
            number = Integer.parseInt(strNumber);
        } catch (Exception e) {
            number = Integer.MIN_VALUE;
        }
        return number;
    }

    //文字列をLocalDate型に変換する
    protected LocalDate toLocalDate(String strDate) {
        if (strDate == null || strDate.equals("")) {
            return LocalDate.now();
        }

        return LocalDate.parse(strDate);
    }

    //リクエストパラメータから引数で指定したパラメータ名の値を返す
    protected String getRequestParam(AttributeConst key) {
        return request.getParameter(key.getValue());
    }

    //リクエストスコープにパラメータを設定する
    protected <V> void putRequestScope(AttributeConst key, V value) {
        request.setAttribute(key.getValue(), value);
    }

    //セッションスコープから指定されたパラメータの値を取得し、返す
    @SuppressWarnings("unchecked")
    protected <R> R getSessionScope(AttributeConst key) {
        return (R) request.getSession().getAttribute(key.getValue());
    }

    //セッションスコープにパラメータを設定する
    protected <V> void putSessionScope(AttributeConst key, V value) {
        request.getSession().setAttribute(key.getValue(), value);
    }

    //セッションスコープから指定されたパラメータを除去する
    protected void removeSessionScope(AttributeConst key) {
        request.getSession().removeAttribute(key.getValue());
    }

    //アプリケーションスコープから指定されたパラメータの値を取得し、返す
    @SuppressWarnings("unchecked")
    protected <R> R getContextScope(PropertyConst key) {
        return (R) context.getAttribute(key.getValue());
    }
}
