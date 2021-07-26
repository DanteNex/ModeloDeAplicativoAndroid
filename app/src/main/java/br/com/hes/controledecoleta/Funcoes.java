package br.com.hes.controledecoleta;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import br.com.hes.controledecoleta.bd.dao.ConfiguracoesDAO;
import br.com.hes.controledecoleta.bd.BD;



public class Funcoes {
    //Arquivo de armazenamento de dados
    public static JSONObject globalResponse;
    public static String ACCESS_TOKEN;
    public static BD db = null;

    public static String BaseAPI(Context context) {
        ConfiguracoesDAO ctxConfs = BD.getDatabase(context).ConfiguracoesDAO();
        if(ctxConfs.trazerConfig("servidor") == null || ctxConfs.trazerConfig("servidor").equals("")) {
            ctxConfs.autocriarConfig();
        }
        return ctxConfs.trazerConfig("servidor");
    }

    public static <T> T defaultIfNull(T one, T two)
    {
        return one != null ? one : two;
    }

    public static void confirmar(Context context, String titulo, String mensagem, String btnConfirmar, String btnCancelar, final Runnable cbSuccess, final Runnable cbCancel) {
        new AlertDialog.Builder(context)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setPositiveButton(btnConfirmar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        cbSuccess.run();
                    }
                }).setNegativeButton(btnCancelar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                cbCancel.run();
            }
        }).setIcon(android.R.drawable.ic_menu_save).show();
    }

    public static void msg(String message, Context contexto) {
        int duracao = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(contexto, message,duracao);
        toast.show();
    }

    public static String formatarPesoLiquido(String peso, String modo) {
        String pesoFinal = "";
        if(modo.equals("novo")) {
            pesoFinal = peso.replace(",", ".");
        } else {
            String parte1 = peso.substring(0, 3);
            String parte2 = peso.substring(3);
            pesoFinal = parte1 + "." + parte2;
        }
        return pesoFinal;
    }

    public static String getSessionToken(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences("br.com.hes.appautomacao", Context.MODE_PRIVATE);
        ACCESS_TOKEN = preferences.getString("session", "");
        return ACCESS_TOKEN;
    }

    /**
     * Post em uma URL
     * @param url url para realizar o request
     * @param method método do request
     * @param postData dados em formato JSONOBject
     * @param context contexto da aplicação
     * @param callback função callback para executar após o request (Também atribui o valor da resposta em Funcoes.globalResponse)
     */
    public static void volleyPost(String url, String method, JSONObject postData, final Context context , final Runnable callback){
        ACCESS_TOKEN = getSessionToken(context);
        JSONObject resData;
        //final JSONObject[] responseText = {new JSONObject()};
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.getCache().clear();
        int reqmet;
        if(method == "GET") {
            reqmet = Request.Method.GET;
        } else {
            reqmet = Request.Method.POST;
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(reqmet, url, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Funcoes.globalResponse = response;
                //responseText[0] = response;
                //Funcoes.msg(new Gson().toJson(response), context);
                callback.run();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Funcoes.msg("Erro." + error.getMessage(), context);
                error.printStackTrace();
            }
        }) {
            //This is for Headers If You Needed

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json;");
                //params.put("Bearer ", ACCESS_TOKEN);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=UTF-8";
            }
            @Override
            public byte[] getBody(){
                byte[] bt = null;
                try {
                    bt = "On".getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return bt;
            }

        };
        int socketTimeout = 0;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    public static class MD5Encode {
        private static String convertedToHex(byte[] data) {
            StringBuilder buf = new StringBuilder();

            for (int i = 0; i < data.length; i++) {
                int halfOfByte = (data[i] >>> 4) & 0x0F;
                int twoHalfBytes = 0;

                do {
                    if ((0 <= halfOfByte) && (halfOfByte <= 9)) {
                        buf.append((char) ('0' + halfOfByte));
                    } else {
                        buf.append((char) ('a' + (halfOfByte - 10)));
                    }

                    halfOfByte = data[i] & 0x0F;

                } while (twoHalfBytes++ < 1);
            }
            return buf.toString();
        }

        public static String MD5(String text) throws NoSuchAlgorithmException,
                UnsupportedEncodingException {
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            byte[] md5 = new byte[64];
            md.update(text.getBytes(StandardCharsets.ISO_8859_1), 0, text.length());
            md5 = md.digest();
            return convertedToHex(md5);
        }
    }

    public static String IfBlank(String verificar, String substituto) {
        return (TextUtils.isEmpty(verificar) ? substituto : verificar);
    }

    public static void atualizarDados() {
        /*
        JSONObject res = Funcoes.globalResponse;
        Funcoes.db.pedidosDAO().limparPedidos();
        Funcoes.db.produtosDAO().limparProdutos();
        try {
            JSONArray pedidosArr = res.getJSONArray("pedidos");
            //loop de pedidos
            for(int i = 0; i < pedidosArr.length(); i++) {
                JSONObject JSONPI = pedidosArr.getJSONObject(i);
                PedidoItem PI = new PedidoItem();
                PI.cliente_nome = JSONPI.getString("cliente_nome");
                PI.codigo_pedido = JSONPI.getString("codigo_pedido");
                //PI.checado =  JSONPI.getBoolean("checado");
                PI.total_produtos = JSONPI.getString("total_produtos");
                //Adiciona o pedido criado na lista de pedidos da classe do banco
                Funcoes.db.pedidosDAO().inserirPedido(PI);
                JSONArray produtosArr = JSONPI.getJSONArray("produtos");
                //loop de produtos
                for(int j = 0; j < produtosArr.length(); j++) {
                    JSONObject JSONPIP = produtosArr.getJSONObject(j);
                    PedidoItemProduto PIP = new PedidoItemProduto();
                    PIP.nome = JSONPIP.getString("nome");
                    PIP.codigo_produto = JSONPIP.getString("codigo_produto");
                    PIP.codigo_pedido = JSONPIP.getString("codigo_pedido");
                    PIP.unidade_venda = JSONPIP.getString("unidade_venda");
                    //adiciona o produto no banco de produtos do pedido
                    Funcoes.db.produtosDAO().inserirProduto(PIP);

                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        */
    }

    public static String numbersToDate(String numbers) {
        return numbers.substring(0, 2) + "/" + numbers.substring(2, 4) + "/" + numbers.substring(4, 6);
    }

    public interface VolleyCallback{
        void onSuccess(String result);
    }

    public static void enviarDados(final Context context, String URL, final String conteudo) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Funcoes.msg("Sucesso ao salvar os dados.", context);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // method to handle errors.
                //Toast.makeText(context, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
                Funcoes.msg(error.getMessage(), context);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // below line we are creating a map for
                // storing our values in key and value pair.
                Map<String, String> params = new HashMap<String, String>();

                // on below line we are passing our key
                // and value pair to our parameters.
                params.put("dados", conteudo.toString());

                // at last we are
                // returning our params.
                return params;
            }

        };
        // below line is to make
        // a json object request.
        queue.add(request);
        //Teste com stringrequest
    }

    public static void salvarDadosNaAPI(final Context context, String idPedido) {
        /*
        List<PedidoItemLote> lotes = Funcoes.db.lotesDAO().trazerLotePorPedido(idPedido);
        Gson gs = new Gson();
        JSONObject wrapper = null;
        JSONArray lts = null;
        try {
            lts = new JSONArray(gs.toJson(lotes));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            wrapper = new JSONObject();
            wrapper.put("lotes", lts);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Enviar dados com stringrequest via parâmetros
        String URL = Funcoes.BaseAPI(context) + context.getString(R.string.url_api_inserir_lotes);
        final JSONObject finalWrapper = wrapper;

        enviarDados(context, URL, finalWrapper.toString());
        */
    }

    public static void trazerDadosAPI(final Context context, String metodo, final Runnable runnable) {
        JSONObject modeloJson = null;
        try {
            modeloJson = new JSONObject("{\"coisa\":123}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(metodo.equals("trazer")) {
            //Se o método for Trazer, significa que deverá ser feito o request na API e atualizar os dados na classe global de Pedidos
            String urlfinal = Funcoes.BaseAPI(context) + context.getString(R.string.url_api_trazer_tudo);
            Funcoes.volleyPost(urlfinal, "GET", modeloJson, context, new Runnable() {
                @Override
                public void run() {
                    runnable.run();

                }
            });
        } else {
            //Transforma a classe global de pedidos em JSON
            Gson PedidosGlobal = new Gson();
            JSONObject pedidosJson = null;
            try {
                pedidosJson = new JSONObject();
            } catch (Exception e) {e.printStackTrace();}
            //Envia o objeto JSON para o backend atualizar
            Funcoes.volleyPost(Funcoes.BaseAPI(context) + context.getString(R.string.url_api_trazer_tudo), "POST", pedidosJson, context, new Runnable() {
                @Override
                public void run() {
                }
            });

        }
    }


}
