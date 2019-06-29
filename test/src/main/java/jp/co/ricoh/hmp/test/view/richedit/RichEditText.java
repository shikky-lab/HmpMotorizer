package jp.co.ricoh.hmp.test.view.richedit;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;


public class RichEditText extends WebView {

    private Context mContext = null;
    private RichEditTextCallback mCallback = null;

    public RichEditText(Context context) {
        super(context, null);
        mContext = context;
    }

    public RichEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setEditTextCallback(RichEditTextCallback callback) {
        mCallback = callback;
    }

    private String generateCSS() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<style type=\"text/css\">");
        stringBuilder.append("body {");
        stringBuilder.append("margin: 0px;");
        stringBuilder.append("background:#F8F8F8 url('../styles/bg.jpg') no-repeat;");
        stringBuilder.append("overflow-x:scroll;");
        stringBuilder.append("}");
        stringBuilder.append(".verticle-mode {");
        stringBuilder.append("writing-mode: tb-rl;");
        stringBuilder.append("-webkit-writing-mode: vertical-rl;");
        stringBuilder.append("writing-mode: vertical-rl;");
        stringBuilder.append("*writing-mode: tb-rl;");
        stringBuilder.append("position: absolute;");
        stringBuilder.append("height: 637px;");
        stringBuilder.append("top: 0px;");
        stringBuilder.append("background-color: #ffffff;");
        stringBuilder.append("z-index:1000;");
        stringBuilder.append("outline:none;");
        stringBuilder.append("word-wrap:break-word;}");
        stringBuilder.append(".fill{");
        stringBuilder.append("right: 0px;");
        stringBuilder.append("left: 0px;");
        stringBuilder.append("overflow-x:scroll;");
        stringBuilder.append("}");
        stringBuilder.append(".left{");
        stringBuilder.append("left: 0px;");
        stringBuilder.append("}");
        stringBuilder.append(".right{");
        stringBuilder.append("right: 0px;");
        stringBuilder.append("}");
        stringBuilder.append("</style>");
        return stringBuilder.toString();
    }

    /*

     */
    private String generateScript() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<script type=\"text/javascript\">");
        stringBuilder.append("function getContent(requestType) {");
        stringBuilder.append("AndroidJS.handleGetContent(requestType,document.getElementById" +
                "(\"editor\")" +
                ".innerHTML);");
        stringBuilder.append("}");
        stringBuilder.append("document.onreadystatechange = function () {");
        stringBuilder.append("if (document.readyState == \"complete\") {");
        stringBuilder.append("var maxDisplayWidth = document.getElementById(\"editor\")" +
                ".offsetWidth;");
        stringBuilder.append("editorClassName = document.getElementById(\"editor\").style" +
                ".width=maxDisplayWidth;");
        stringBuilder.append("}");
        stringBuilder.append("};");
        stringBuilder.append("</script>");
        return stringBuilder.toString();
    }

    private String generateHtml(String html) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<!DOCTYPE html>");
        stringBuilder.append("<html>");
        stringBuilder.append("<head>");
        stringBuilder.append("<meta charset=\"utf-8\"/>");
        stringBuilder.append("<meta name=\"viewport\"");
        stringBuilder.append("content=\"width=device-width,initial-scale=1.0, minimum-scale=1.0, " +
                "maximum-scale=1.0, user-scalable=no\"/>");
        stringBuilder.append("<title></title>");
        stringBuilder.append(generateCSS());
        stringBuilder.append(generateScript());
        stringBuilder.append("</head>");
        stringBuilder.append("<body>");
        stringBuilder.append("<div id=\"editor\" class=\"verticle-mode fill\" " +
                "contenteditable=\"true\" placeholder=\"body\">" + html + "</div>");
        stringBuilder.append("</body>");
        stringBuilder.append("</html>");
        return stringBuilder.toString();
    }

    private void initialize(String html) {

        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setStandardFontFamily("Default");
        addJavascriptInterface(this, "AndroidJS");
        String data = generateHtml(html);
        loadData("", "text/html", "utf-8");
        loadData(data, "text/html", "utf-8");
    }

    public void getContent(String requestType) {
        loadUrl("javascript:getContent('" + requestType + "')");
    }

    @JavascriptInterface
    public void handleGetContent(String requestType, String content) {
        mCallback.onGetContentCompleted(requestType, content);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initialize("");
    }
}
