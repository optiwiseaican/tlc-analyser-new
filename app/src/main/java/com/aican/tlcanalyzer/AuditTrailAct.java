package com.aican.tlcanalyzer;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.tlcanalyzer.adapterClasses.PdfAdapter;
import com.aican.tlcanalyzer.customClasses.LegacyTableView;
import com.aican.tlcanalyzer.database.UsersDatabase;
import com.aican.tlcanalyzer.databinding.ActivityAuditTrailBinding;
import com.aican.tlcanalyzer.interfaces.OnPDFSelectListener;
import com.aican.tlcanalyzer.utils.Source;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.DottedBorder;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class AuditTrailAct extends AppCompatActivity implements OnPDFSelectListener {

    private ActivityAuditTrailBinding binding;
    private UsersDatabase usersDatabase;
    private String month;
    private String year;
    int STORAGE_PERMISSION_REQUEST_CODE = 1;
    String startDateString, endDateString, startTimeString, endTimeString, arNumString, batchNumString, compoundName;

    LegacyTableView legacyTableView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuditTrailBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        legacyTableView = findViewById(R.id.legacy_table_view);

        usersDatabase = new UsersDatabase(this);

        Cursor res = usersDatabase.get_userActivity_data();
        if (res != null) {
            if (res.moveToFirst()) {
                year = res.getString(0).substring(0, 4);
                month = res.getString(0).substring(5, 7);
                binding.dateA.setText("Data available from " + res.getString(0));
            }
        } else {
            binding.dateA.setText("No data available");
            month = "01";
            year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        }

        if (Build.VERSION.SDK_INT >= 33) {
            String[] permissions = new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // Permissions are not granted, request them
                ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                // Permissions are already granted, proceed with using external storage
                // Your code for accessing external storage goes here
                displayPDF();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Permissions are not granted, request them
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                // Permissions are already granted, proceed with using external storage
                // Your code for accessing external storage goes here
                displayPDF();
            }
        }


        binding.customDates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Calendar calendar1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

                // now set the starting bound from current month to
                // previous MARCH
                if (month.equals("01")) {
                    calendar1.set(Calendar.MONTH, Calendar.JANUARY);

                } else if (month.equals("02")) {
                    calendar1.set(Calendar.MONTH, Calendar.FEBRUARY);

                } else if (month.equals("03")) {
                    calendar1.set(Calendar.MONTH, Calendar.MARCH);

                } else if (month.equals("04")) {
                    calendar1.set(Calendar.MONTH, Calendar.APRIL);

                } else if (month.equals("05")) {
                    calendar1.set(Calendar.MONTH, Calendar.MAY);

                } else if (month.equals("06")) {
                    calendar1.set(Calendar.MONTH, Calendar.JUNE);

                } else if (month.equals("07")) {
                    calendar1.set(Calendar.MONTH, Calendar.JULY);

                } else if (month.equals("08")) {
                    calendar1.set(Calendar.MONTH, Calendar.AUGUST);

                } else if (month.equals("09")) {
                    calendar1.set(Calendar.MONTH, Calendar.SEPTEMBER);

                } else if (month.equals("10")) {
                    calendar1.set(Calendar.MONTH, Calendar.OCTOBER);

                } else if (month.equals("11")) {
                    calendar1.set(Calendar.MONTH, Calendar.NOVEMBER);

                } else if (month.equals("12")) {
                    calendar1.set(Calendar.MONTH, Calendar.DECEMBER);

                } else {
                    calendar1.set(Calendar.MONTH, Calendar.JANUARY);

                }
//            calendar1.set(Calendar.DATE, 7);

                long start = calendar1.getTimeInMillis();

                // now set the ending bound from current month to
                // DECEMBER
                calendar1.set(Calendar.MONTH, Calendar.DECEMBER);
                long end = calendar1.getTimeInMillis();

                CalendarConstraints.Builder calendarConstraintBuilder = new CalendarConstraints.Builder();
                calendarConstraintBuilder.setStart(start);
                calendarConstraintBuilder.setEnd(end);


                MaterialDatePicker datePicker =
                        MaterialDatePicker.Builder.dateRangePicker()
                                .setSelection(new Pair(MaterialDatePicker.thisMonthInUtcMilliseconds(),
                                        MaterialDatePicker.todayInUtcMilliseconds()))
                                .setTitleText("Select dates")
                                .setCalendarConstraints(calendarConstraintBuilder.build())
                                .build();
                datePicker.show(getSupportFragmentManager(), "date");

                datePicker.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>) selection -> {
                    Long startDate = selection.first;
                    Long endDate = selection.second;
                    startDateString = android.text.format.DateFormat.format("yyyy-MM-dd", new Date(startDate)).toString();
                    endDateString = android.text.format.DateFormat.format("yyyy-MM-dd", new Date(endDate)).toString();
                    String date1 = "Start: " + startDateString + " End: " + endDateString;
                    Toast.makeText(AuditTrailAct.this, date1.toString(), Toast.LENGTH_SHORT).show();

                    binding.dateRangeText.setText(date1);

                    plotTable();

                });


            }
        });

        File exportDir = new File(getExternalFilesDir(null).toString() + "/" + "Users Activity Files");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//        ) != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(
//                        this,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Permissions are not granted, request them
//            ActivityCompat.requestPermissions(
//                    this,
//                    new String[]{
//                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    },
//                    STORAGE_PERMISSION_REQUEST_CODE
//            );
//        } else {
//            // Permissions are already granted, proceed with using external storage
//            // Your code for accessing external storage goes here
//
//            Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show();
//            displayPDF();
//        }


        binding.exportUserData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    generatePDF(startDateString, endDateString);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                }

//                displayPDF();
            }
        });


        binding.allExports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AuditTrailAct.this, AllAuditTrailExports.class));
            }
        });

        plotTable();

    }


    private void plotTable() {
        LegacyTableView.insertLegacyTitle("ID", "date", "time", "name", "role", "activities", "projectName", "projectType");

        SQLiteDatabase db = usersDatabase.getWritableDatabase();
        Cursor calibCSV = db.rawQuery("SELECT * FROM UserLogDetails", null);
        if (startDateString != null && endDateString != null) {
            calibCSV = db.rawQuery("SELECT * FROM UserLogDetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "')", null);
        } else {
            calibCSV = db.rawQuery("SELECT * FROM UserLogDetails", null);
        }

        int i = 1;
        while (calibCSV.moveToNext()) {
            String date = calibCSV.getString(calibCSV.getColumnIndex("date"));
            String time = calibCSV.getString(calibCSV.getColumnIndex("time"));
            String name = calibCSV.getString(calibCSV.getColumnIndex("name"));
            String role = calibCSV.getString(calibCSV.getColumnIndex("role"));
            String activities = calibCSV.getString(calibCSV.getColumnIndex("activities"));
            String projectName = calibCSV.getString(calibCSV.getColumnIndex("projectName"));
            String projectID = calibCSV.getString(calibCSV.getColumnIndex("projectID"));
            String projectType = calibCSV.getString(calibCSV.getColumnIndex("projectType"));

            LegacyTableView.insertLegacyContent(
                    i + "", date + "", time + "", name + "", role + "", activities + "", projectName + "", projectType + ""

            );
            i++;
        }

        legacyTableView.setTheme(LegacyTableView.CUSTOM);
        legacyTableView.setContent(LegacyTableView.readLegacyContent());
        legacyTableView.setTitle(LegacyTableView.readLegacyTitle());
//        legacyTableView.setBottomShadowVisible(true);
        legacyTableView.setHighlight(LegacyTableView.ODD);
        legacyTableView.setBottomShadowVisible(false);
        legacyTableView.setFooterTextAlignment(LegacyTableView.CENTER);
        legacyTableView.setTableFooterTextSize(5);
        legacyTableView.setTableFooterTextColor("#f0f0ff");
        legacyTableView.setTitleTextAlignment(LegacyTableView.CENTER);
        legacyTableView.setContentTextAlignment(LegacyTableView.CENTER);
        legacyTableView.setTablePadding(20);
        legacyTableView.setBackgroundOddColor("#F0F0FF");
        legacyTableView.setHeaderBackgroundLinearGradientBOTTOM("#F0F0FF");
        legacyTableView.setHeaderBackgroundLinearGradientTOP("#F0F0FF");
        legacyTableView.setBorderSolidColor("#f0f0ff");
        legacyTableView.setTitleTextColor("#212121");
        legacyTableView.setTitleFont(LegacyTableView.BOLD);
        legacyTableView.setZoomEnabled(false);
        legacyTableView.setShowZoomControls(false);

        legacyTableView.setContentTextColor("#000000");
        legacyTableView.build();
    }


    private void handleGeneratedPDF(Uri generatedFile) {
        displayPDF();
    }

    private void showError(String message) {
        Log.e("ErrorWhile", message);
//        Toast.makeText(this, "Error : " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, proceed with using external storage
                displayPDF();
            } else {
                Source.toast(this, "We need permission to access these files");
                // Permission is denied, handle accordingly (e.g., show a message or disable functionality)
            }
        }
    }

    ArrayList<File> pdfList;
    PdfAdapter pdfAdapter;

    private void displayPDF() {
        RecyclerView recyclerView = findViewById(R.id.recView);
        recyclerView.setHasFixedSize(true);
        pdfList = new ArrayList<>();
        pdfList.addAll(findPDF(getApplicationContext().getExternalFilesDir(getString(R.string.folderLocation2))));
        pdfAdapter = new PdfAdapter(this, pdfList, this);
        recyclerView.setAdapter(pdfAdapter);
        pdfAdapter.notifyDataSetChanged();
    }

    public ArrayList<File> findPDF(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();
        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                arrayList.addAll(findPDF(singleFile));
            } else {
                if (singleFile.getName().endsWith(".pdf")) {
                    arrayList.add(singleFile);
                }
            }
        }
        return arrayList;
    }

    private Uri generatePDF(String startDateString, String endDateString) throws FileNotFoundException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        File file = new File(getExternalFilesDir(null).toString() + File.separator +
                "Users Activity Files/USER_ACT_REPORT_" + startDateString + "_to_" + endDateString + "_" + System.currentTimeMillis() +
                ".pdf");

        OutputStream outputStream = new FileOutputStream(file);
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        String company_name = "N/A";
        String user_name = "N/A";


        try {
            DeviceRgb textColor = new DeviceRgb(255f, 0f, 0f);
            float fontSize = 16f;

            document.add(new
                            Paragraph(
                            "Order No. : " + currentDateandTime
                    )
            );

            Text coloredText = new Text("Report's PDF")
                    .setFontColor(textColor)
                    .setFontSize(fontSize);

            document.add(new Paragraph(coloredText).setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(""));
//            document.add(new Chunk(new DottedLineSeparator()));
            Paragraph dottedLine = new Paragraph()
                    .setBorder(new
                                    DottedBorder(
                                    1f
                            )
                    ) // Color and width of the border
                    .setMarginTop(5f) // Distance between text and line
                    .setMarginBottom(5f);// Distance between line and next element
            document.add(dottedLine);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            Paragraph chunk1 = new Paragraph(new
                    Text("Report of " + "User Activities from " + startDateString + " to " + endDateString)
                    .setFontColor(DeviceGray.BLACK)
                    .setFontSize(fontSize)
                    .setFont(StandardFonts.HELVETICA)
                    .setTextAlignment(TextAlignment.CENTER)
            );

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
//            document.add(new Chunk(lineSeparator));
            document.add(new Paragraph(" "));


            float columnWidth[] = {150f, 210f, 190f, 170f, 210f, 340f, 210f, 210f};


            Table table = new Table(columnWidth);
            table.addCell("Id");
            table.addCell("date");
            table.addCell("time");
            table.addCell("name");
            table.addCell("role");
            table.addCell("activities");
            table.addCell("projectName");
//            table.addCell("projectID");
            table.addCell("projectType");

            SQLiteDatabase db = usersDatabase.getWritableDatabase();
            Cursor calibCSV = db.rawQuery("SELECT * FROM UserLogDetails", null);
            if (startDateString != null && endDateString != null) {
                calibCSV = db.rawQuery("SELECT * FROM UserLogDetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "')", null);
            } else {
                calibCSV = db.rawQuery("SELECT * FROM UserLogDetails", null);
            }

            int i = 1;
            while (calibCSV.moveToNext()) {
                String date = calibCSV.getString(calibCSV.getColumnIndex("date"));
                String time = calibCSV.getString(calibCSV.getColumnIndex("time"));
                String name = calibCSV.getString(calibCSV.getColumnIndex("name"));
                String role = calibCSV.getString(calibCSV.getColumnIndex("role"));
                String activities = calibCSV.getString(calibCSV.getColumnIndex("activities"));
                String projectName = calibCSV.getString(calibCSV.getColumnIndex("projectName"));
                String projectID = calibCSV.getString(calibCSV.getColumnIndex("projectID"));
                String projectType = calibCSV.getString(calibCSV.getColumnIndex("projectType"));

                table.addCell(i + "");
                table.addCell(date + "");
                table.addCell(time + "");
                table.addCell(name + "");
                table.addCell(role + "");
                table.addCell(activities + "");
                table.addCell(projectName + "");
//                table.addCell(projectID + "");
                table.addCell(projectType + "");
                i++;
            }
            document.add(table);

            document.close();
            Uri path = Uri.parse(file.getPath());

            Intent ing = new Intent(this, PDFActivity.class);
            ing.putExtra("path", path.toString());
            startActivity(ing);

            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Uri path = Uri.parse(file.getPath());

        return path;
    }

    private String getPresentDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    @Override
    public void onPDFSelected(@Nullable File file, @NonNull String fileName, int position) {
        Intent intent = new Intent(AuditTrailAct.this, PDFActivity.class);
        intent.putExtra("path", file.getAbsolutePath());
        intent.putExtra("flag", "n");
        intent.putExtra("fileName", fileName);
        startActivity(intent);
    }

    @Override
    public void onDelete(File file, int position) {
        File fearFiles = new File(file.getAbsolutePath());
        boolean deleted = fearFiles.delete();
        if (deleted) {
            Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error - Please try again", Toast.LENGTH_SHORT).show();
        }
        displayPDF();
    }

    @Override
    public void inExternalApp(File file, Context context) {
        try {
            Intent target = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (file.getName().endsWith(".pdf") || file.getName().endsWith(".pptx")) {
                    target.setDataAndType(
                            FileProvider.getUriForFile(
                                    context,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    file
                            ), "application/pdf"
                    );
                }
                if (file.getName().endsWith(".png") || file.getName().endsWith(".jpeg") || file.getName().endsWith(".jpg")) {
                    target.setDataAndType(
                            FileProvider.getUriForFile(
                                    context,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    file
                            ), "image/*"
                    );
                }
                if (file.getName().endsWith(".docx") || file.getName().endsWith(".docs") || file.getName().endsWith(".txt")) {
                    target.setDataAndType(
                            FileProvider.getUriForFile(
                                    context,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    file
                            ), "text/plain"
                    );
                }
                if (file.getName().endsWith(".sce")) {
                    target.setDataAndType(
                            FileProvider.getUriForFile(
                                    context,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    file
                            ), "*/*"
                    );
                }
            } else {
                target.setDataAndType(Uri.fromFile(file), "*/*");
            }
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            target.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intent = Intent.createChooser(target, "Open File");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "No apk found to open this file", Toast.LENGTH_SHORT).show();
                // Instruct the user to install a PDF reader here, or something
            }
        } catch (Exception e) {
            Log.d("ErrorGot", e.getMessage());
        }
    }
}
