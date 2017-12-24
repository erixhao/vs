package com.vs.dao;


import com.vs.common.domain.HistoricalData;
import com.vs.common.domain.enums.TimePeriod;
import com.vs.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by erix-mac on 16/1/10.
 */
@Repository
@Slf4j
public class MarketDataDAO {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class MarketDataMapper implements RowMapper<HistoricalData> {
        public HistoricalData mapRow(ResultSet rs, int rowNum) throws SQLException {
            HistoricalData data = new HistoricalData();

            data.setPeriod(TimePeriod.DAILY);
            data.setCode(rs.getString("CODE"));
            data.setName(rs.getString("NAME"));
            data.setDate(rs.getDate("MKT_DATE"));
            data.setOpen(rs.getDouble("PRICE_OPEN"));
            data.setHigh(rs.getDouble("PRICE_HIGH"));
            data.setLow(rs.getDouble("PRICE_LOW"));
            data.setClose(rs.getDouble("PRICE_CLOSE"));
            data.setVolume(rs.getLong("VOLUME"));
            data.setVolumeAmount(rs.getDouble("VOLUME_AMT"));

            return data;
        }
    }

    public List<HistoricalData> getAllMarketDataByYears(String years) {
        String sql = "SELECT ID,CODE,NAME,MKT_DATE,PRICE_OPEN,PRICE_HIGH,PRICE_LOW, PRICE_CLOSE,VOLUME,VOLUME_AMT FROM MKT_DAILY_DATA WHERE  MKT_DATE >= ? ORDER BY MKT_DATE ASC";
        //System.out.println("------------------ SQL : " + sql);
        return this.jdbcTemplate.query(sql, new Object[]{years}, new MarketDataMapper());
    }

    public List<HistoricalData> getAllMarketDataByYears(String code, String years) {
        String sql = "SELECT ID,CODE,NAME,MKT_DATE,PRICE_OPEN,PRICE_HIGH,PRICE_LOW, PRICE_CLOSE,VOLUME,VOLUME_AMT FROM MKT_DAILY_DATA WHERE CODE=? AND MKT_DATE >= ? ORDER BY MKT_DATE ASC";
        //System.out.println("------------------ SQL : " + sql);
        return this.jdbcTemplate.query(sql, new Object[]{code, years}, new MarketDataMapper());
    }

    public List<HistoricalData> getAllMarketDataByYears(List<String> codes, String years) {
        String sql = "SELECT ID,CODE,NAME,MKT_DATE,PRICE_OPEN,PRICE_HIGH,PRICE_LOW, PRICE_CLOSE,VOLUME,VOLUME_AMT FROM MKT_DAILY_DATA WHERE CODE in (:codes) AND MKT_DATE >= :years ORDER BY MKT_DATE ASC";

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.jdbcTemplate);
        MapSqlParameterSource sqlParameter= new MapSqlParameterSource();
        sqlParameter.addValue("codes", codes);
        sqlParameter.addValue("years", years);

        //System.out.println("------------------ SQL : " + sql);
        return namedParameterJdbcTemplate.query(sql, sqlParameter, new MarketDataMapper() );
    }

    public List<HistoricalData> getAllMarketData(String code) {
        return this.jdbcTemplate.query("SELECT ID,CODE,NAME,MKT_DATE,PRICE_OPEN,PRICE_HIGH,PRICE_LOW, PRICE_CLOSE,VOLUME,VOLUME_AMT FROM MKT_DAILY_DATA WHERE CODE=? ORDER BY MKT_DATE ASC", new Object[]{code}, new MarketDataMapper());
    }

    public HistoricalData getLatestMarketData(String code){
        String sql = "SELECT * FROM MKT_DAILY_DATA WHERE CODE = ? ORDER BY MKT_DATE DESC limit 1";
        return this.jdbcTemplate.queryForObject(sql,new Object[]{code}, new MarketDataMapper());
    }



    public void insert(final List<HistoricalData> datas){
        String sql = "insert into MKT_DAILY_DATA(CODE,NAME,MKT_DATE,PRICE_OPEN,PRICE_HIGH,PRICE_LOW, PRICE_CLOSE,VOLUME,VOLUME_AMT,UPDATE_DATE) VALUES(?,?,?,?,?,?,?,?,?,?) ON duplicate KEY UPDATE UPDATE_DATE=?";

        this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                HistoricalData d = datas.get(i);

                ps.setString(1,d.getCode());
                ps.setString(2,d.getName());
                ps.setDate(3, Date.valueOf(d.timeZoneDate()));
                ps.setDouble(4, d.getOpen());
                ps.setDouble(5,d.getHigh());
                ps.setDouble(6,d.getLow());
                ps.setDouble(7,d.getClose());
                ps.setLong(8,d.getVolume());
                ps.setDouble(9,d.getVolumeAmount());
                ps.setDate(10, new Date(d.getUpdateDate().getTime()));
                ps.setDate(11, new Date(d.getUpdateDate().getTime()));

            }

            @Override
            public int getBatchSize() {
                return datas.size();
            }
        });
    }


    public int getMarketCount(String code) {
        return this.jdbcTemplate.queryForObject("select count(*) from MKT_DAILY_DATA WHERE CODE=?", new Object[]{code}, Integer.class);
    }

    public int getMarketCount(String code, String date){
        return this.jdbcTemplate.queryForInt("SELECT count(1) FROM MKT_DAILY_DATA WHERE CODE=? AND MKT_DATE=?",new Object[]{code, date});
    }

    public List<String> getAllExistingCodes(){
        return this.jdbcTemplate.queryForList("SELECT DISTINCT CODE FROM MKT_DAILY_DATA",String.class);

    }

    public static void main(String[] args) {

        System.out.println(Date.valueOf(DateUtils.withTimeZoneFormat(new java.util.Date())).toString());

        /*MarketDataDAO dao = BeanContext.getBean(MarketDataDAO.class);

        List<String> codes = Lists.newArrayList("600030","000001s");
        List<HistoricalData> datas = dao.getAllMarketDataByYears(codes,"2016-01-01");
        System.out.println("Count: " + datas.size());*/

    }
}
