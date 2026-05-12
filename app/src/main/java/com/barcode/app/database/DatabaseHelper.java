package com.barcode.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.barcode.app.model.Product;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "products.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "products";
    private static final String COL_BARCODE = "barcode";
    private static final String COL_NAME = "name";
    private static final String COL_PRICE = "price";

    private SQLiteDatabase cachedDb;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.enableWriteAheadLogging();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + COL_BARCODE + " TEXT PRIMARY KEY, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_PRICE + " REAL NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    private SQLiteDatabase db() {
        if (cachedDb == null || !cachedDb.isOpen()) {
            cachedDb = getWritableDatabase();
        }
        return cachedDb;
    }

    public Product getProduct(String barcode) {
        Cursor cursor = db().query(TABLE, null, COL_BARCODE + "=?",
                new String[]{barcode}, null, null, null);
        Product product = null;
        if (cursor.moveToFirst()) {
            product = new Product(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getDouble(2)
            );
        }
        cursor.close();
        return product;
    }

    public void saveProduct(Product product) {
        ContentValues values = new ContentValues(3);
        values.put(COL_BARCODE, product.getBarcode());
        values.put(COL_NAME, product.getName());
        values.put(COL_PRICE, product.getPrice());
        db().insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateProduct(Product product) {
        ContentValues values = new ContentValues(2);
        values.put(COL_NAME, product.getName());
        values.put(COL_PRICE, product.getPrice());
        db().update(TABLE, values, COL_BARCODE + "=?",
                new String[]{product.getBarcode()});
    }

    public void deleteProduct(String barcode) {
        db().delete(TABLE, COL_BARCODE + "=?", new String[]{barcode});
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        Cursor cursor = db().query(TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            list.add(new Product(cursor.getString(0), cursor.getString(1), cursor.getDouble(2)));
        }
        cursor.close();
        return list;
    }

    public int importFromCsv(File file) {
        int count = 0;
        SQLiteDatabase database = db();
        database.beginTransaction();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return 0;

            ContentValues values = new ContentValues(3);
            String line;
            while ((line = reader.readLine()) != null) {
                int i1 = line.indexOf(',');
                if (i1 < 0) continue;
                int i2 = line.indexOf(',', i1 + 1);

                String barcode = line.substring(0, i1).trim();
                String name = i2 < 0 ? line.substring(i1 + 1).trim()
                        : line.substring(i1 + 1, i2).trim();
                double price = 0;
                if (i2 >= 0) {
                    try { price = Double.parseDouble(line.substring(i2 + 1).trim()); }
                    catch (NumberFormatException ignored) {}
                }
                if (!barcode.isEmpty() && !name.isEmpty()) {
                    values.clear();
                    values.put(COL_BARCODE, barcode);
                    values.put(COL_NAME, name);
                    values.put(COL_PRICE, price);
                    database.insertWithOnConflict(TABLE, null, values,
                            SQLiteDatabase.CONFLICT_REPLACE);
                    count++;
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
        return count;
    }

    public int exportToCsv(File file) {
        List<Product> products = getAllProducts();
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8")) {
            writer.write("﻿barcode,name,price\n");
            StringBuilder sb = new StringBuilder(64);
            for (Product p : products) {
                sb.setLength(0);
                sb.append(p.getBarcode()).append(',')
                  .append(p.getName()).append(',')
                  .append(p.getPrice()).append('\n');
                writer.write(sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products.size();
    }
}
