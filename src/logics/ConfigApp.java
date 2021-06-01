package logics;

public class ConfigApp {
    // -1 не визначений користувач, 0 покупець, 1 продавець, 2 адміністратор;
    public static int type_user_session = -1;
    public static boolean isStatusDB_Connect = false;
    //-=-=-=-=-=-=-//
    public static String dbHost = "[HOST_TO_DB]";
    public static String dbUser = "[DB_USER]";
    public static String dbPassword = "[DB_PASSWORD]";
    public static String dbName = "[DB_NAME]";
    //-=-=-=-=-=-=-//
    public static final String TABLE_USERS = "users";
    public static final String TABLE_SPARE_PARTS = "spare_parts";
    public static final String TABLE_BOOKED_USERS = "booked_users";
    public static final String TABLE_LOGS = "logs";
    public static final String TABLE_REVIEWS = "reviews";
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_BOUGHTS = "boughts";
    public static final String TABLE_STATISTICS = "statistics";
    //-=-=-=-=-=-=-//
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String MAIL = "mail";
    public static final String PASSWORD = "password";
    public static final String TYPE_USER = "type_user";
    public static final String AVATAR = "avatar";

    public static final String IMG = "img";
    public static final String TITLE = "title";
    public static final String PRICE = "price";
    public static final String COUNT = "count";
    public static final String USER_ID = "user_id";
    public static final String DESCRIPTION = "description";

    public static final String COMPUTER_NAME = "computer_name";
    public static final String TEXT = "text";
    public static final String DATE = "date";

    public static final String PRODUCT_ID = "product_id";
    public static final String BUY_USER_ID = "buy_user_id";

    public static final String NOTE = "note";

    public static final String STATISTICS_DATA = "statistics_data";
    //-=-=-=-=-=-=-//
    public static final String J_COUNT_PUBLICATION = "count_publication";
    public static final String J_COUNT_REVIEWS = "count_reviews";
    public static final String J_COUNT_ORDERS = "count_orders";
    public static final String J_COUNT_BOUGHTS = "count_boughts";
    public static final String J_COUNT_DELETE_ORDERS = "count_delete_orders";
    public static final String J_COUNT_DELETE_PUBLICATION = "count_delete_publication";

}
