package br.com.hes.controledecoleta.bd.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;


@Dao
public interface ConfiguracoesDAO {
    @Query("SELECT valor_config FROM Configuracoes WHERE nome_config = :nome")
    String trazerConfig(String nome);

    @Query("UPDATE Configuracoes SET valor_config = :valor WHERE nome_config = :nome")
    void setarConfig(String nome, String valor);

    @Query("INSERT INTO Configuracoes (nome_config, valor_config) VALUES (\"servidor\", \"https://192.168.110.21:5001\")")
    void autocriarConfig();

}
