package java_kur_projesi;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Java_kur_projesi {
    private String xml_adresi ="temp\\h.xml" ;
    private String veritabani_adresi ="temp\\kurlar.db";
    
    private String tarih;//="";
    private Connection conn = null;
    private PreparedStatement pstmt;
    private Statement stmt;
  
    public Java_kur_projesi()
    {
         try {
            // db parameters
            String url = "jdbc:sqlite:"+veritabani_adresi;
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            
            System.out.println("Connection to SQLite has been established.");
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public static void xml_indir() throws MalformedURLException, IOException
    {
        URL url = new URL("https://www.tcmb.gov.tr/kurlar/today.xml");    
        BufferedInputStream  TampondanOkuyucu= new BufferedInputStream(url.openStream());     
        BufferedOutputStream TampondanYazici = new BufferedOutputStream(new FileOutputStream("temp\\h.xml"));             
        byte i = 0;
        do
        {         
            i = (byte)TampondanOkuyucu.read();
            TampondanYazici.write(i);
        }
        while (i != -1);    
        TampondanOkuyucu.close();
        TampondanYazici.close();      
        System.out.println("Dosya basarıyla kayıt edildi");  
    }
    public void veritabani_update() throws SQLException
    {
        String sorgu = "UPDATE tablo_kurlar Set sts_kur_tl = sts_kur_tl/sts_kur_birim_unit,sts_kur_satis_tl= sts_kur_satis_tl/sts_kur_birim_unit,sts_kur_ef_alis_tl= sts_kur_ef_alis_tl/sts_kur_birim_unit,sts_kur_ef_satis_tl= sts_kur_ef_satis_tl/sts_kur_birim_unit";
        pstmt = conn.prepareStatement(sorgu);
        pstmt.executeUpdate();
        
        Double dolarin_degeri = null;
        sorgu = "Select sts_kur_tl from tablo_kurlar where sts_kur_birim='USD'";
        stmt = conn.createStatement();
        ResultSet rs=stmt.executeQuery(sorgu);
        while(rs.next())
        {
            dolarin_degeri =  rs.getDouble(1);
        }
        Double euronun_degeri = null;
        sorgu = "Select sts_kur_tl from tablo_kurlar where sts_kur_birim='EUR'";
        stmt = conn.createStatement();
        ResultSet rs_=stmt.executeQuery(sorgu);
        while(rs_.next())
        {
            euronun_degeri =  rs_.getDouble(1);
        }
        //String sorgu;
        sorgu = "Update tablo_kurlar set sts_kur_dolar = round(?/sts_kur_tl,5),sts_kur_euro = round(?/sts_kur_tl,5),sts_kur_baz_doviz = sts_kur_tl";
        pstmt = conn.prepareStatement(sorgu);
        pstmt.setDouble(1, dolarin_degeri);
        pstmt.setDouble(2, euronun_degeri);
        pstmt.executeUpdate();    
    }
    public void tabloyu_sil() throws SQLException
    {
        String sorgu = "Delete from tablo_kurlar";
        pstmt = conn.prepareStatement(sorgu);
        pstmt.executeUpdate();
    }
    public void veritabanina_aktar() throws FileNotFoundException, IOException, SQLException
    {
        xml_indir();
        tabloyu_sil();
        
        File dosya = new File(xml_adresi);
        String satir;
        FileReader fileReader = new FileReader(dosya);
        BufferedReader br = new BufferedReader(fileReader);
        
        int pozisyon = 0;
        int pozisyon2 = 0;
        
        String kod="",isim="",CurrencyName="",gecici="";
        int unit=0;
        double ForexBuying=0,ForexSelling=0,BanknoteBuying=0,BanknoteSelling=0,CrossRateUSD=0,CrossRateOther=0;
        while ((satir = br.readLine()) != null) 
        {
            unit = 0;
            if(satir.indexOf(" Tarih")>0)
            {
                pozisyon = satir.indexOf(" Tarih");  //19
                pozisyon2 = satir.indexOf(" Date"); //29
                pozisyon2 = pozisyon2 - 1;  //28
                pozisyon = pozisyon + 8;   //27
                tarih = satir.substring(pozisyon,pozisyon2);
            }
            if (satir.indexOf("Kod") > 0)
            {
                pozisyon = satir.indexOf("Kod");
                pozisyon2 = satir.indexOf("CurrencyCode");
                kod = satir.substring(pozisyon + 5, pozisyon2-2);
                satir = br.readLine();
                if(satir.indexOf("<Unit>")>0)
                {
                    pozisyon = satir.indexOf("<Unit>");
                    pozisyon2 = satir.indexOf("</Unit>");
                    unit = Integer.parseInt(satir.substring(pozisyon+6 , pozisyon2));
                }
                satir = br.readLine();
                if (satir.indexOf("<Isim>") > 0)
                {
                    pozisyon = satir.indexOf("<Isim>");
                    pozisyon2 = satir.indexOf("</Isim>");
                    isim = satir.substring(pozisyon + 6, pozisyon2);
                }
                satir = br.readLine();
                if (satir.indexOf("<CurrencyName>") > 0)
                {
                    pozisyon = satir.indexOf("<CurrencyName>");
                    pozisyon2 = satir.indexOf("</CurrencyName>");
                    CurrencyName = satir.substring(pozisyon + 14, pozisyon2);
                }
                satir = br.readLine();
                if (satir.indexOf("<ForexBuying>") > 0)
                {
                    pozisyon = satir.indexOf("<ForexBuying>");
                    pozisyon2 = satir.indexOf("</ForexBuying>");
                    gecici = satir.substring(pozisyon + 13, pozisyon2);
                    ForexBuying = Double.parseDouble(gecici);
                }
                satir = br.readLine();
                if (satir.indexOf("<ForexSelling>") > 0)
                {
                    pozisyon = satir.indexOf("<ForexSelling>");
                    pozisyon2 = satir.indexOf("</ForexSelling>");
                    gecici = satir.substring(pozisyon + 14, pozisyon2);
                    ForexSelling = Double.parseDouble(gecici);
                }
                satir = br.readLine();
                if (satir.indexOf("<BanknoteBuying>") > 0)
                {
                    gecici="";
                    pozisyon = satir.indexOf("<BanknoteBuying>");
                    pozisyon2 = satir.indexOf("</BanknoteBuying>");
                    gecici = satir.substring(pozisyon + 16, pozisyon2);
                    if (gecici.length()==0)
                    {
                        gecici = "0";
                    }
                   BanknoteBuying = Double.parseDouble(gecici);
                }
                satir = br.readLine();
                if (satir.indexOf("<BanknoteSelling>") > 0)
                {
                    pozisyon = satir.indexOf("<BanknoteSelling>");
                    pozisyon2 = satir.indexOf("</BanknoteSelling>");
                    gecici = satir.substring(pozisyon + 17, pozisyon2);
                    if (gecici.length()==0)
                    {
                        gecici = "0";
                    }
                    BanknoteSelling = Double.parseDouble(gecici);
                }
                satir = br.readLine();
                if (satir.indexOf("<CrossRateUSD>") > 0)
                {
                    pozisyon = satir.indexOf("<CrossRateUSD>");
                    pozisyon2 = satir.indexOf("</CrossRateUSD>");
                    gecici = satir.substring(pozisyon + 14, pozisyon2);
                    CrossRateUSD = Double.parseDouble(gecici);
                }
                else
                {
                    CrossRateUSD = 0;
                }
                satir = br.readLine();
                if (satir.indexOf("<CrossRateOther>") > 0)
                {
                    pozisyon = satir.indexOf("<CrossRateOther>");
                    pozisyon2 = satir.indexOf("</CrossRateOther>");
                    gecici = satir.substring(pozisyon + 16, pozisyon2);
                    CrossRateOther = Double.parseDouble(gecici);
                }
                else
                {
                    CrossRateOther = 0;
                }
                String sql = "INSERT INTO tablo_kurlar (sts_kur_birim,sts_kur_tarih,sts_kur_tl,sts_kur_satis_tl,sts_kur_ef_alis_tl,sts_kur_ef_satis_tl,sts_kur_doviz_cinsi,sts_kur_birim_unit) VALUES (?,?,?,?,?,?,?,?)";                
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, kod);
                pstmt.setString(2, tarih);
                pstmt.setDouble(3, ForexBuying);
                pstmt.setDouble(4, ForexSelling);
                pstmt.setDouble(5, BanknoteBuying);
                pstmt.setDouble(6, BanknoteSelling);
                pstmt.setString(7, isim);
                pstmt.setDouble(8, unit);
                pstmt.executeUpdate();
            }    
        }
        br.close();
        veritabani_update();
    }
    public String[][] verileri_al() throws SQLException
    {
        String sql ="Select * from tablo_kurlar"; 
        stmt = conn.createStatement();
        ResultSet rs=stmt.executeQuery(sql);
        String tablo_sonuc[][] = new String[20][9];
        int i=0;
        while(rs.next())
        {
            tablo_sonuc[i][0] = rs.getString(1);
            tablo_sonuc[i][1] = rs.getString(2);
            tablo_sonuc[i][2] = rs.getString(3);
            tablo_sonuc[i][3] = rs.getString(4);
            tablo_sonuc[i][4] = rs.getString(5);
            tablo_sonuc[i][5] = rs.getString(6);
            tablo_sonuc[i][6] = rs.getString(7);
            tablo_sonuc[i][7] = rs.getString(8);
            tablo_sonuc[i][8] = rs.getString(9);
            i = i + 1;
        }
        return tablo_sonuc;  
    }
    public String getTarih() {
        return tarih;
    }

    public void setTarih(String tarih) {
        this.tarih = tarih;
    }
}
