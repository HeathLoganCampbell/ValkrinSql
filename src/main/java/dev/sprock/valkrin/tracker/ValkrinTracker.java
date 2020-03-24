package dev.sprock.valkrin.tracker;

import dev.sprock.valkrin.Valkrin;
import dev.sprock.valkrin.commons.Result;

public class ValkrinTracker<C>
{
    private final Class<C> clazz;
    private final String tableName;
    private final Valkrin valkrin;

    public ValkrinTracker(Class<C> clazz, String tableName, Valkrin valkrin)
    {
        this.clazz = clazz;
        this.tableName = tableName;
        this.valkrin = valkrin;

        this.createTable();
    }

    public String getQuery(Query query)
    {
        switch (query)
        {
            case CREATE_TABLE:
                return "CREATE TABLE IF NOT EXISTS `" + tableName + "` ( `data_id` INT NOT NULL AUTO_INCREMENT , `identifier` VARCHAR(36) NOT NULL ,  `data` TEXT NOT NULL,  PRIMARY KEY (`data_id`), UNIQUE (`identifier`)) ENGINE = MyISAM;";
            case SELECT:
                return "SELECT * FROM `" + tableName + "` WHERE `identifier`= ?";
            case UPDATE:
                return "UPDATE `" + tableName + "` SET data=? WHERE `identifier`= ?";
            case CREATE:
                return "INSERT INTO `" + tableName + "` (`identifier`, `data` ) VALUES (?, ?);";
        }
        return "";
    }

    public void createTable()
    {
        valkrin.execute(getQuery(Query.CREATE_TABLE), (prepare) -> {});
    }

    public void save(String identifierKey, Object thingToSave)
    {
        String rawJson = Valkrin.GSON.toJson(thingToSave);
        valkrin.getResult(
                    getQuery(Query.SELECT),
                    (preparedStatement) -> { preparedStatement.setString(1, identifierKey); },
                    (rs) -> {
                        if(rs.next())
                        {
                            //update
                            valkrin.execute(getQuery(Query.UPDATE), (prepare) -> {
                                prepare.setString(1, rawJson);
                                prepare.setString(2, identifierKey);
                            });
                        }
                        else
                        {
                            //Create
                            valkrin.execute(getQuery(Query.CREATE), (prepare) -> {
                                prepare.setString(1, identifierKey);
                                prepare.setString(2, rawJson);
                            });
                        }
                    }
                );
    }

    public C load(String identifierKey)
    {
        Result<C> result = new Result<>();
        valkrin.getResult(
                getQuery(Query.SELECT),
                (preparedStatement) -> { preparedStatement.setString(1, identifierKey); },
                (rs) -> {
                    if(rs.next())
                    {
                        String input = rs.getString("data");
                        C object = Valkrin.GSON.fromJson(input, this.clazz);
                        result.setResult(object);
                    }
                });
        return result.getResult();
    }

    public enum Query
    {
        CREATE_TABLE, CREATE, UPDATE, SELECT, DELETE;
    }
}
