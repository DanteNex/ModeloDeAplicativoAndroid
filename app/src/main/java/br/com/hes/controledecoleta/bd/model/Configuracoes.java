package br.com.hes.controledecoleta.bd.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Configuracoes {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    public int codigo_config;

    public String nome_config;
    public String valor_config;
}
