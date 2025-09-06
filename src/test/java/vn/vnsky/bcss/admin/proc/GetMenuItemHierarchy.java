package vn.vnsky.bcss.admin.proc;

import java.sql.*;

public class GetMenuItemHierarchy {

    @SuppressWarnings("unused")
    public static ResultSet call(Connection connection, String userId, String appCode, Object refCursor) throws SQLException {
        PreparedStatement statement;
        ResultSet rs;
        statement = connection.prepareStatement("select ID from OAUTH2_REGISTERED_CLIENT where CLIENT_ID = ?");
        statement.setString(1, appCode);
        rs = statement.executeQuery();
        String appId = null;
        if (rs.next()) {
            appId = rs.getString("ID");
        }
        statement.close();
        if (userId == null) {
            statement = connection.prepareStatement("""
                WITH RECURSIVE cte(ID, CODE, NAME, URI, ICON, PARENT_ID, LEVEL, PATH, ORDINAL_PATH) AS (
                SELECT S1.ID, S1.CODE, S1.NAME, S1.URI, S1.ICON, S1.PARENT_ID, 1,
                       S1.ID AS PATH, CASEWHEN(S1.ORDINAL IS NULL, '00', TO_CHAR(S1.ORDINAL, 'FM00')) AS ORDINAL_PATH
                FROM OBJECT S1
                WHERE PARENT_ID IS NULL AND APP_ID = ? AND STATUS = 1
                UNION ALL
                SELECT S2.ID, S2.CODE, S2.NAME, S2.URI, S2.ICON, S2.PARENT_ID, cte.LEVEL + 1,
                       CONCAT(cte.PATH, '-', S2.ID), CONCAT(cte.ORDINAL_PATH, '-', CASEWHEN(S2.ORDINAL IS NULL, '00', TO_CHAR(S2.ORDINAL, 'FM00'))) AS ORDINAL_PATH
                FROM cte INNER JOIN OBJECT S2 on cte.ID = S2.PARENT_ID
                WHERE APP_ID = ? AND STATUS = 1
            )
            SELECT cte.ID, CODE, NAME, URI, ICON, PARENT_ID, LEVEL, PATH, RIGHTS.JOINED_ACTIONS
            FROM cte
                LEFT JOIN (
                SELECT O.ID,
                       LISTAGG(DISTINCT A.CODE, ',') WITHIN GROUP (ORDER BY A.CODE) AS JOINED_ACTIONS
                FROM OBJECT O
                         INNER JOIN OBJECT_ACTION OA ON O.ID = OA.OBJECT_ID
                         INNER JOIN ACTION A ON OA.ACTION_ID = A.ID
                GROUP BY O.ID
            ) AS RIGHTS ON CTE.ID = RIGHTS.ID
            ORDER BY cte.ORDINAL_PATH
        """);
            statement.setString(1, appId);
            statement.setString(2, appId);
        } else {
            statement = connection.prepareStatement("""
                WITH RECURSIVE cte(ID, CODE, NAME, URI, ICON, PARENT_ID, LEVEL, PATH, ORDINAL_PATH) AS (
                    SELECT S1.ID, S1.CODE, S1.NAME, S1.URI, S1.ICON, S1.PARENT_ID, 1,
                           S1.ID AS PATH, CASEWHEN(S1.ORDINAL IS NULL, '00', TO_CHAR(S1.ORDINAL, 'FM00')) AS ORDINAL_PATH
                    FROM OBJECT S1
                    WHERE PARENT_ID IS NULL AND APP_ID = ? AND STATUS = 1
                    UNION ALL
                    SELECT S2.ID, S2.CODE, S2.NAME, S2.URI, S2.ICON, S2.PARENT_ID, cte.LEVEL + 1,
                           CONCAT(cte.PATH, '-', S2.ID), CONCAT(cte.ORDINAL_PATH, '-', CASEWHEN(S2.ORDINAL IS NULL, '00', TO_CHAR(S2.ORDINAL, 'FM00'))) AS ORDINAL_PATH
                    FROM cte INNER JOIN OBJECT S2 on cte.ID = S2.PARENT_ID
                    WHERE APP_ID = ? AND STATUS = 1
                )
                SELECT cte.ID, CODE, NAME, URI, ICON, PARENT_ID, LEVEL, PATH, RIGHTS.JOINED_ACTIONS
                FROM cte
                LEFT JOIN (
                    WITH MERGED_ROLE_USER AS (
                            SELECT RU.ROLE_ID, RU.USER_ID FROM ROLE_USER RU WHERE RU.USER_ID = ?
                            UNION ALL
                            SELECT RG.ROLE_ID, GU.USER_ID FROM GROUP_ROLE RG
                                                                   INNER JOIN GROUP_USER GU ON RG.GROUP_ID = GU.GROUP_ID
                            WHERE GU.USER_ID = ?
                        )
                    SELECT ROA.OBJECT_ID,
                           LISTAGG(DISTINCT A.CODE, ',') WITHIN GROUP (ORDER BY A.CODE) AS JOINED_ACTIONS
                    FROM ROLE_OBJECT_ACTION ROA
                        INNER JOIN OBJECT O ON ROA.OBJECT_ID = O.ID
                        INNER JOIN ACTION A ON ROA.ACTION_ID = A.ID
                        INNER JOIN MERGED_ROLE_USER RU ON ROA.ROLE_ID = RU.ROLE_ID
                        INNER JOIN ROLE R ON ROA.ROLE_ID = R.ID
                    WHERE R.STATUS = 1
                    GROUP BY ROA.OBJECT_ID
                    ) AS RIGHTS ON CTE.ID = RIGHTS.ID
                    ORDER BY cte.ORDINAL_PATH
            """);
            statement.setString(1, appId);
            statement.setString(2, appId);
            statement.setString(3, userId);
            statement.setString(4, userId);
        }
        return statement.executeQuery();
    }

}
