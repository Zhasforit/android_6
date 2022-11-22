package kz.talipovsn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class MySQLite extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1; // НОМЕР ВЕРСИИ БАЗЫ ДАННЫХ И ТАБЛИЦ !

    static final String DATABASE_NAME = "software"; // Имя базы данных

    static final String TABLE_NAME = "softwa"; // Имя таблицы
    static final String ID = "id"; // Поле с ID
    static final String NAME = "name"; // Поле с наименованием организации
    static final String NAME_LC = "name_lc"; // // Поле с наименованием организации в нижнем регистре
    static final String PHONE_NUMBER = "phone_number";
    static final String ADDRESS = "address";
    static final String WEBSITE = "website";
    static final String RATING = "rating";// Поле с телефонным номером

    static final String ASSETS_FILE_NAME = "software.txt"; // Имя файла из ресурсов с данными для БД
    static final String DATA_SEPARATOR = "|"; // Разделитель данных в файле ресурсов с телефонами

    private Context context; // Контекст приложения

    public MySQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Метод создания базы данных и таблиц в ней
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY,"
                + NAME + " TEXT,"
                + NAME_LC + " TEXT,"
                + PHONE_NUMBER + " TEXT,"
                + ADDRESS + " TEXT,"
                + WEBSITE + " TEXT,"
                + RATING + " INTEGER"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        System.out.println(CREATE_CONTACTS_TABLE);
        loadDataFromAsset(context, ASSETS_FILE_NAME,  db);
    }

    // Метод при обновлении структуры базы данных и/или таблиц в ней
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        System.out.println("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Добавление нового контакта в БД
    public void addData(SQLiteDatabase db, String name, String phoneNumber, String address, String website, String rating) {
        ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(NAME_LC, name.toLowerCase());
        values.put(PHONE_NUMBER, phoneNumber);
        values.put(ADDRESS, address);
        values.put(WEBSITE, website);
        values.put(RATING, rating);
        db.insert(TABLE_NAME, null, values);
    }

    // Добавление записей в базу данных из файла ресурсов
    public void loadDataFromAsset(Context context, String fileName, SQLiteDatabase db) {
        BufferedReader in = null;

        try {
            // Открываем поток для работы с файлом с исходными данными
            InputStream is = context.getAssets().open(fileName);
            // Открываем буфер обмена для потока работы с файлом с исходными данными
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            while ((str = in.readLine()) != null) { // Читаем строку из файла
                String strTrim = str.trim(); // Убираем у строки пробелы с концов
                if (!strTrim.equals("")) { // Если строка не пустая, то
                    StringTokenizer st = new StringTokenizer(strTrim, DATA_SEPARATOR); // Нарезаем ее на части
                    String name = st.nextToken().trim(); // Извлекаем из строки название организации без пробелов на концах
                    String phoneNumber = st.nextToken().trim();
                    String address = st.nextToken().trim();
                    String website = st.nextToken().trim();
                    String rating = st.nextToken().trim();// Извлекаем из строки номер организации без пробелов на концах
                    addData(db, name, phoneNumber, address, website, rating); // Добавляем название и телефон в базу данных
                }
            }

        // Обработчики ошибок
        } catch (IOException ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    // Получение значений данных из БД в виде строки с фильтром
    public String getData(String filter, Spinner spinner) {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + NAME; // Переменная для SQL-запроса
        long s = spinner.getSelectedItemId();
        if (filter.contains("'")) {
            selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 0" ;
        } else if (s == 0) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + NAME_LC + " LIKE '%" +
                    filter.toLowerCase() + "%'"
                    + " OR " + PHONE_NUMBER + " LIKE '%" + filter + "%'"
                    + " OR " + ADDRESS + " LIKE '%" + filter + "%'"
                    + " OR " + WEBSITE + " LIKE '%" + filter + "%'"
                    + " OR " + RATING + " LIKE '%" + filter + "%'" + ")";
        } else if(s == 1){
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + NAME_LC + " LIKE '%" +
                    filter.toLowerCase() + "%'" + ")";
        } else if (s == 2){
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + PHONE_NUMBER + " LIKE '%" +
                    filter.toLowerCase() + "%'" + ")";
        } else if (s == 3){
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + ADDRESS + " LIKE '%" +
                    filter.toLowerCase() + "%'" + ")";
        } else if (s == 4){
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + WEBSITE + " LIKE '%" +
                    filter.toLowerCase() + "%'" + ")";
        } else if (s == 5){
            if (filter.isEmpty() | !filter.matches("[-+]?\\d")) {
                selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 0" ;
            } else {
                selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + RATING + " >= " + Integer.parseInt(filter) + " ORDER BY " + RATING;
            }
        }
        SQLiteDatabase db = this.getReadableDatabase(); // Доступ к БД
        Cursor cursor = db.rawQuery(selectQuery, null); // Выполнение SQL-запроса

        StringBuilder data = new StringBuilder(); // Переменная для формирования данных из запроса

        int num = 0;
        if (cursor.moveToFirst()) { // Если есть хоть одна запись, то
            do { // Цикл по всем записям результата запроса
                int n = cursor.getColumnIndex(NAME);
                int t = cursor.getColumnIndex(PHONE_NUMBER);
                int a = cursor.getColumnIndex(ADDRESS);
                int w = cursor.getColumnIndex(WEBSITE);
                int r = cursor.getColumnIndex(RATING);
                String name = cursor.getString(n); // Чтение названия организации
                String phoneNumber = cursor.getString(t); // Чтение телефонного номера
                String address = cursor.getString(a);
                String website = cursor.getString(w);
                String rating = cursor.getString(r);
                data.append(String.valueOf(++num) + ") Компания:" + name + "\n Телефон: " + phoneNumber + "\n Адрес: " + address + "\n Сайт: " + website + "\n Рейтинг: " + rating + "\n");
            } while (cursor.moveToNext()); // Цикл пока есть следующая запись
        }
        return data.toString(); // Возвращение результата
    }

}