package br.com.hes.controledecoleta.bd;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import br.com.hes.controledecoleta.bd.dao.ConfiguracoesDAO;
import br.com.hes.controledecoleta.bd.model.Configuracoes;

@Database(entities = {Configuracoes.class}, version = 26, exportSchema = false)
public abstract class BD extends RoomDatabase {
    //Variaveis
    private static BD INSTANCE;
    public static String DATABASE_NAME = "ControleDeColeta";

    public abstract ConfiguracoesDAO ConfiguracoesDAO();

    //Metodos
    public static BD getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, BD.class, DATABASE_NAME)
                    .allowMainThreadQueries().fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }


    public static void destroyInstance() {
        INSTANCE = null;
    }
}
