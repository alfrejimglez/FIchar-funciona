package com.fichar.fichar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String SHARED_PREFS_FILE = "com.fichar.fichar.SHARED_PREFS";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private WebView webView;
    private SharedPreferences sharedPreferences;
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener SharedPreferences
        sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        // Obtener los valores guardados
        username = sharedPreferences.getString(KEY_USERNAME, "");
        password = sharedPreferences.getString(KEY_PASSWORD, "");

        // Verificar si hoy es un día laborable (lunes a viernes) antes de cargar la página
        if (isWeekday()) {
            if(username.isEmpty() || password.isEmpty()) {
                showInputDialog();
            } else {
                setupWebView();
                loadWebPage("https://presence.addingplus.net/default.aspx");
            }
        } else {
            Toast.makeText(this, "Hoy no es un día laborable", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView = new WebView(this);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        webSettings.setUserAgentString(userAgent);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String jsCode = "javascript:document.getElementById('username').value = '" + username + "';" +
                        "document.getElementById('pwd').value = '" + password + "';" +
                        "document.getElementById('btnEntrar').click();";

                webView.evaluateJavascript(jsCode, null);

                // Agregar un retraso de medio segundo antes de ejecutar el siguiente código
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String firstJsCode = "javascript:document.getElementById('ContentPlaceHolder1_ucUsuariosFichaje_User1_btnIniciar').click();";
                        webView.evaluateJavascript(firstJsCode, null);

                        // Agregar un retraso de 150 milisegundos antes de ejecutar el siguiente código
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                String secondJsCode = "javascript:document.getElementById('ContentPlaceHolder1_ucUsuariosFichaje_User1_ucFichajeUser1_btnIniciar').click();";
                                webView.evaluateJavascript(secondJsCode, null);
                            }
                        }, 110); // 190 mili
                    }
                }, 130); // 200 mili


            }

        });

        // Configurar el WebView como contenido de la actividad
        setContentView(webView);
    }

    private void loadWebPage(String url) {
        webView.loadUrl(url);
    }

    private boolean isWeekday() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Verificar si el día ES de lunes (Calendar.MONDAY) a viernes (Calendar.FRIDAY)
        return dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY;
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Establecer el título del diálogo con un icono de información
        TextView title = new TextView(this);
        title.setText("Ingrese tus datos de fichar");
        title.setPadding(10, 10, 10, 10);
        title.setTextSize(20);
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, 0, 0);
        title.setCompoundDrawablePadding(20);
        builder.setCustomTitle(title);

        // Establecer el diseño del diálogo
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Crear EditTexts para nombre de usuario y contraseña
        final EditText editTextUsername = new EditText(this);
        editTextUsername.setHint("Usuario");
        layout.addView(editTextUsername);

        final EditText editTextPassword = new EditText(this);
        editTextPassword.setHint("Contraseña");
        editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(editTextPassword);
        builder.setView(layout);

        // Establecer botón de "Aceptar"
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Guardar los valores en SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_USERNAME, editTextUsername.getText().toString());
                editor.putString(KEY_PASSWORD, editTextPassword.getText().toString());
                editor.apply();

                // AQUI QUIERO QUE SE REINICIE
                // Reiniciar la aplicación
                Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        });

        // Mostrar el diálogo
        builder.show();
    }
}
