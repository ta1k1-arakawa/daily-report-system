package services;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.NoResultException;

import actions.views.EmployeeConverter;
import actions.views.EmployeeView;
import constants.JpaConst;
import models.Employee;
import models.validators.EmployeeValidator;
import utils.EncryptUtil;

//従業員テーブルの操作に関わる処理を行うクラス
public class EmployeeService extends ServiceBase {
    //指定されたページ数の一覧画面に表示するデータを取得し、EmployeeViewのリストで返す
    public List<EmployeeView> getPerPage(int page){
        List<Employee> employees = em.createNamedQuery(JpaConst.Q_EMP_GET_ALL, Employee.class)
                .setFirstResult(JpaConst.ROW_PER_PAGE * (page - 1))
                .setMaxResults(JpaConst.ROW_PER_PAGE)
                .getResultList();

        return EmployeeConverter.toViewList(employees);
    }

    //従業員テーブルのデータ件数を取得し、返す
    public long countAll() {
        long empCount = (long) em.createNamedQuery(JpaConst.Q_EMP_COUNT, Long.class)
                .getSingleResult();

        return empCount;
    }

    //社員番号、パスワードを条件に取得したデータをEmployeeViewのインスタンスで返す
    public EmployeeView findOne(String code, String plainPass, String pepper) {
        Employee e = null;
        try {
            //パスワードをハッシュ化
            String pass = EncryptUtil.getPasswordEncrypt(plainPass, pepper);

            //社員番号とハッシュ化済みパスワードを条件に未削除の従業員1名を取得する
            e = em.createNamedQuery(JpaConst.Q_EMP_GET_BY_CODE_AND_PASS, Employee.class)
                    .setParameter(JpaConst.JPQL_PARM_CODE, code)
                    .setParameter(JpaConst.JPQL_PARM_PASSWORD, pass)
                    .getSingleResult();
        } catch (NoResultException ex) {
        }

        return EmployeeConverter.toView(e);
    }

    //idを条件に取得したデータをEmployeeViewのインスタンスで返す
    public EmployeeView findOne(int id) {
        Employee e = findOneInternal(id);
        return EmployeeConverter.toView(e);
    }

    //社員番号を条件に該当するデータ件数を取得し、返す
    public long countByCode(String code) {
        //指定した社員番号を保持する従業員の件数を取得する
        long employees_count = (long) em.createNamedQuery(JpaConst.Q_EMP_COUNT_REGISTERED_BY_CODE, Long.class)
                .setParameter(JpaConst.JPQL_PARM_CODE, code)
                .getSingleResult();
        return employees_count;
    }

    //画面から入力された従業員の登録内容を元にデータを1件作成し、従業員テーブルに登録する
    public List<String> create(EmployeeView ev, String pepper){
        //パスワードをハッシュ化
        String pass = EncryptUtil.getPasswordEncrypt(ev.getPassword(), pepper);
        ev.setPassword(pass);

        //登録日時、更新日時は現在時刻を設定する
        LocalDateTime now = LocalDateTime.now();
        ev.setCreatedAt(now);
        ev.setUpdatedAt(now);

        //登録内容のバリデーションを行う
        List<String> errors = EmployeeValidator.validate(this, ev, true, true);

        //バリデーションエラーがなければデータを登録する
        if(errors.size() == 0) {
            create(ev);
        }

        //エラーを返す
        return errors;
    }

    //画面から入力された授業員の更新内容を元にデータを1件作成し、従業員テーブルを更新する
    public List<String> update(EmployeeView ev, String pepper) {
        //idを条件に登録済みの従業員情報を取得する
        EmployeeView savedEmp = findOne(ev.getId());

        boolean validateCode = false;
        if(!savedEmp.getCode().equals(ev.getCode()) ) {
            //社員番号を更新するとき
            //社員番号についてのバリデーションを行う
            validateCode = true;
            //変更後の社員番号を設定する
            savedEmp.setCode(ev.getCode());
        }

        boolean validatePass = false;
        if(ev.getPassword() != null && !ev.getPassword().equals("")) {
            //パスワードに入力があるとき
            //パスワードについてバリデーションを行う
            validatePass = true;
            //変更後のパスワードをハッシュ化し設定する
            savedEmp.setPassword(
                    EncryptUtil.getPasswordEncrypt(ev.getPassword(), pepper));
        }

        //変更後の氏名を設定する
        savedEmp.setName(ev.getName());
        //変更後の管理者フラグを設定する
        savedEmp.setAdminFlag(ev.getAdminFlag());

        //更新日時に現在時刻を設定する
        LocalDateTime today = LocalDateTime.now();
        savedEmp.setUpdatedAt(today);

        //更新内容にバリデーションを行う
        List<String> errors = EmployeeValidator.validate(this, savedEmp, validateCode, validatePass);


        //バリデーションエラーがなければデータを更新する
        if(errors.size() == 0) {
            update(savedEmp);
        }

        //エラーを返す
        return errors;
    }

    //idを条件に従業員データを論理削除する
    public void destroy(Integer id) {
        //idを条件に登録済みの従業員情報を取得する
        EmployeeView savedEmp = findOne(id);

        //更新日時に現在時刻を設定する
        LocalDateTime today = LocalDateTime.now();
        savedEmp.setUpdatedAt(today);

        //論理削除フラグをたてる
        savedEmp.setDeleteFlag(JpaConst.EMP_DEL_TRUE);

        //更新処理を行う
        update(savedEmp);
    }

    //社員番号とパスワードを条件に検索し、データが取得できるかどうかで認証結果を返す
    public Boolean validateLogin(String code, String plainPass, String pepper) {
        boolean isValidEmployee = false;
        if(code != null && !code.equals("") && plainPass != null && !plainPass.equals("")) {
            EmployeeView ev = findOne(code, plainPass, pepper);
            if(ev != null && ev.getId() != null) {
                //データが取得できたとき、認証成功
                isValidEmployee = true;
            }
        }

        //認証結果を返す
        return isValidEmployee;
    }

    //idを条件にデータを1件取得し、Employeeのインスタンスで返す
    private Employee findOneInternal(int id) {
        Employee e = em.find(Employee.class, id);

        return e;
    }

    //従業員データを1件登録する
    private void create(EmployeeView ev) {
        em.getTransaction().begin();
        em.persist(EmployeeConverter.toModel(ev));
        em.getTransaction().commit();
    }

    //従業員データを更新する
    private void update(EmployeeView ev) {
        em.getTransaction().begin();
        Employee e = findOneInternal(ev.getId());
        EmployeeConverter.copyViewToModel(e, ev);
        em.getTransaction().commit();
    }
}
