package br.edu.ifba.tcc.core.services;

// Define quantas consultas sequenciais cada requisição faz ao banco (parâmetro queries): 1 = cliente, 2 = cliente + pedidos, 3 a 12 = cliente + pedidos + itens dos primeiros pedidos
public final class QueryIntensity {

    public static final int MIN = 1;
    public static final int DEFAULT = 3;
    public static final int MAX = 12;

    private QueryIntensity() {
    }

    /**
     * @param queries Quantidade de consultas pedida na requisição
     * @return Verdadeiro quando a quantidade está entre 1 e 12
     */
    public static boolean isValid(int queries) {
        return queries >= MIN && queries <= MAX;
    }

    /**
     * @param queries Quantidade de consultas pedida na requisição
     */
    // Lança IllegalArgumentException quando está fora de 1 a 12, que o GlobalExceptionHandler transforma em 400
    public static void requireValid(int queries) {
        if (!isValid(queries)) {
            throw new IllegalArgumentException("queries deve estar entre " + MIN + " e " + MAX);
        }
    }

    /**
     * @param queries Quantidade de consultas pedida na requisição
     * @return Verdadeiro quando a requisição precisa buscar os pedidos (2
     * consultas ou mais)
     */
    public static boolean fetchesOrders(int queries) {
        return queries >= 2;
    }

    /**
     * @param queries Quantidade de consultas pedida na requisição
     * @param availableOrders Quantidade de pedidos que o cliente tem
     * @return Quantos pedidos terão os itens buscados, fica 0 quando a
     * requisição tem menos de 3 consultas
     */
    public static int ordersWithItems(int queries, int availableOrders) {
        // Tira as 2 primeiras consultas (cliente e pedidos) e nunca passa da quantidade de pedidos existentes
        return Math.min(Math.max(queries - 2, 0), availableOrders);
    }
}
