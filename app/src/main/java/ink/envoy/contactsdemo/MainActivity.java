package ink.envoy.contactsdemo;

import android.app.Activity;
import android.database.SQLException;
import android.os.Bundle;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.listeners.TableDataLongClickListener;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import ink.envoy.contactsdemo.util.ContactsDataAccessor;
import ink.envoy.contactsdemo.util.ContactsDatabaseHelper;
import ink.envoy.contactsdemo.util.MultiInputMaterialDialogBuilder;
import ink.envoy.contactsdemo.view.HiddenFloatingActionsMenu;

public class MainActivity extends AppCompatActivity {

    private final int SNACKBAR_SHOW_TIME = 2000;
    private FloatingActionButton addActionButton;
    private FloatingActionButton multiAddActionButton;
    private FloatingActionButton clearActionButton;
    private FloatingActionButton sqlActionButton;
    private SortableTableView contactsTableView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initialize();
    }

    private void initialize() {
        bindViews();
        setupTableHeader();
        setupTableDataClickListeners();
        loadData();
        setupActionButtons();
    }

    private void bindViews() {
        addActionButton = (FloatingActionButton) findViewById(R.id.addActionButton);
        multiAddActionButton = (FloatingActionButton) findViewById(R.id.multiAddActionButton);
        clearActionButton = (FloatingActionButton) findViewById(R.id.clearActionButton);
        sqlActionButton = (FloatingActionButton) findViewById(R.id.sqlActionButton);
        contactsTableView = (SortableTableView) findViewById(R.id.contactsTableView);
    }

    private void setupTableHeader() {
        // setup header title
        contactsTableView.setHeaderAdapter(new SimpleTableHeaderAdapter(getApplicationContext(),
                "#",
                "姓名",
                "手机",
                "邮箱"
        ));

        // setup header weight model
        TableColumnWeightModel columnModel = new TableColumnWeightModel(4);
        columnModel.setColumnWeight(0, 2);
        columnModel.setColumnWeight(1, 4);
        columnModel.setColumnWeight(2, 7);
        columnModel.setColumnWeight(3, 9);
        contactsTableView.setColumnModel(columnModel);

        // setup header comparators
        for (int i = 1; i < 4; ++i) {
            final int currentIndex = i;
            contactsTableView.setColumnComparator(i, new Comparator<String[]>() {
                @Override
                public int compare(String[] obj1, String[] obj2) {
                    return obj1[currentIndex].compareTo(obj2[currentIndex]);
                }
            });
        }
    }

    private void setupTableDataClickListeners() {
        final Activity thisActivity = this;
        final HiddenFloatingActionsMenu actionsMenu = (HiddenFloatingActionsMenu) findViewById(R.id.actionsMenu);

        contactsTableView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionsMenu.collapse();
            }
        });

        contactsTableView.addDataLongClickListener(new TableDataLongClickListener<String[]>() {
            @Override
            public boolean onDataLongClicked(int rowIndex, final String[] clickedData) {
                actionsMenu.collapse();
                new MaterialDialog.Builder(thisActivity)
                        .title("要删除联系人吗？")
                        .content("被删除的联系人将无法恢复。")
                        .positiveText("删除")
                        .negativeText("取消")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                // delete the specified contact info
                                new ContactsDataAccessor(getApplicationContext()).delete(Integer.parseInt(clickedData[0]));

                                // refresh
                                loadData();
                            }
                        })
                        .show();

                return false;
            }
        });
    }

    private void loadData() {
        List<String[]> queryResult = new ContactsDataAccessor(getApplicationContext()).get();
        if (!queryResult.isEmpty()) {
            contactsTableView.setVisibility(View.VISIBLE);
            contactsTableView.setDataAdapter(new SimpleTableDataAdapter(getApplicationContext(), queryResult));
        } else {
            contactsTableView.setVisibility(View.GONE);
        }
    }

    private void showTextOnSnackbar(View view, final HiddenFloatingActionsMenu actionsMenu, String text) {
        // toggle actions menu button
        actionsMenu.show(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                actionsMenu.show(true);
            }
        }, SNACKBAR_SHOW_TIME + 500);

        // show successful feedback
        Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE).setDuration(SNACKBAR_SHOW_TIME)
                .setAction("好的", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {}
                }).show();
    }

    private void setupActionButtons() {
        setupAddButton();
        setupMultiAddButton();
        setupClearButton();
        setupSqlButton();
    }

    private void setupAddButton() {
        final Activity thisActivity = this;
        final HiddenFloatingActionsMenu actionsMenu = (HiddenFloatingActionsMenu) findViewById(R.id.actionsMenu);
        addActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                actionsMenu.collapse();
                new MultiInputMaterialDialogBuilder(thisActivity)
                        .addInput(InputType.TYPE_CLASS_TEXT, "", "姓名")
                        .addInput(InputType.TYPE_CLASS_PHONE, "", "手机号")
                        .addInput(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, "", "邮箱")
                        .inputs(new MultiInputMaterialDialogBuilder.InputsCallback() {
                            @Override
                            public void onInputs(MaterialDialog dialog, List<CharSequence> inputs, boolean allInputsValidated) {
                                // save to DB
                                List<String> list = new LinkedList<>();
                                for (CharSequence input : inputs) {
                                    list.add(input.toString());
                                }
                                new ContactsDataAccessor(getApplicationContext()).put(list.toArray(new String[list.size()]));

                                // refresh
                                loadData();

                                // feedback
                                showTextOnSnackbar(view, actionsMenu, "添加成功");
                            }
                        })
                        .title("添加联系人")
                        .positiveText("添加")
                        .negativeText("取消")
                        .show();
            }
        });
    }

    private void setupMultiAddButton() {
        final Activity thisActivity = this;
        final HiddenFloatingActionsMenu actionsMenu = (HiddenFloatingActionsMenu) findViewById(R.id.actionsMenu);
        multiAddActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                actionsMenu.collapse();
                new MaterialDialog.Builder(thisActivity)
                        .title("批量添加联系人")
                        .content("请输入您要添加的多个联系人，格式为：姓名 手机 邮箱。多个联系人用分号隔开，如：小明 13651340231 xm@mail.com; 小红 13961234152 xh@mail.com")
                        .input("", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {}
                        })
                        .positiveText("添加")
                        .negativeText("取消")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                // parse user input into readable data
                                String rawContactInfos = dialog.getInputEditText().getText().toString();
                                final Pattern pattern = Pattern.compile("\\s?(\\S+)\\s(\\S+)\\s([^;\\s]+)\\s?;?");
                                Matcher matcher = pattern.matcher(rawContactInfos);

                                // retrieve contact inf
                                boolean ok = true;
                                List<String[]> contactInfos = new LinkedList<>();
                                while (matcher.find()) {
                                    if (matcher.groupCount() != 3) {
                                        ok = false;
                                        break;
                                    }
                                    contactInfos.add(new String[] {matcher.group(1), matcher.group(2), matcher.group(3)});
                                }
                                if (ok && !contactInfos.isEmpty()) {
                                    // save to DB
                                    new ContactsDataAccessor(getApplicationContext()).put(contactInfos);
                                    // refresh
                                    loadData();
                                    // feedback
                                    showTextOnSnackbar(view, actionsMenu, "添加成功");
                                } else {
                                    showTextOnSnackbar(view, actionsMenu, "输入有误，添加失败");
                                }

                            }
                        })
                        .show();
            }
        });
    }

    private void setupClearButton() {
        final Activity thisActivity = this;
        final HiddenFloatingActionsMenu actionsMenu = (HiddenFloatingActionsMenu) findViewById(R.id.actionsMenu);
        clearActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                actionsMenu.collapse();
                new MaterialDialog.Builder(thisActivity)
                        .title("确认要清空吗？")
                        .content("被删除的联系人将无法恢复。")
                        .positiveText("清空")
                        .negativeText("取消")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                // clear DB
                                new ContactsDataAccessor(getApplicationContext()).clear();

                                // refresh
                                loadData();

                                // feedback
                                showTextOnSnackbar(view, actionsMenu, "已清空");
                            }
                        })
                        .show();
            }
        });
    }

    private void setupSqlButton() {
        final Activity thisActivity = this;
        final HiddenFloatingActionsMenu actionsMenu = (HiddenFloatingActionsMenu) findViewById(R.id.actionsMenu);
        sqlActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                actionsMenu.collapse();
                new MaterialDialog.Builder(thisActivity)
                        .title("输入SQL语句")
                        .content("如希望获得查询结果，请点击查询，否则点击更新。\n表名：contacts\n编号：_id\n姓名：name\n手机：phone\n邮箱：email")
                        .input("", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {}
                        })
                        .positiveText("查询")
                        .neutralText("取消")
                        .negativeText("更新")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                try {
                                    // exec sql
                                    List<String[]> result = new ContactsDataAccessor(getApplicationContext())
                                            .execQueryingSql(dialog.getInputEditText().getText().toString());

                                    // retrieve readable result
                                    StringBuilder answer = new StringBuilder();
                                    if (!result.isEmpty()) {
                                        answer.append("编号 | 姓名 | 手机 | 邮箱\n");
                                        for (String[] contactInfos : result) {
                                            for (String contactInfo : contactInfos) {
                                                answer = answer.append(contactInfo).append(" ");
                                            }
                                            answer = answer.append("\n");
                                        }
                                    } else {
                                        answer = new StringBuilder("空");
                                    }

                                    // show result
                                    new MaterialDialog.Builder(thisActivity)
                                            .title("查询结果")
                                            .content(answer.toString())
                                            .positiveText("完成")
                                            .show();

                                } catch (SQLException e) {
                                    showTextOnSnackbar(view, actionsMenu, "SQL语法错误，查询失败");
                                }
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                try {
                                    // exec sql
                                    new ContactsDataAccessor(getApplicationContext())
                                            .execSql(dialog.getInputEditText().getText().toString());

                                    // refresh
                                    loadData();

                                    // feedback
                                    showTextOnSnackbar(view, actionsMenu, "更新成功");

                                } catch (SQLException e) {
                                    showTextOnSnackbar(view, actionsMenu, "SQL语法错误，更新失败");
                                }
                            }
                        })
                        .show();
            }
        });
    }
}
