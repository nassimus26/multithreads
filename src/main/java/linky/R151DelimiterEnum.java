package linky;

import linky.api.IDelimiter;

public enum R151DelimiterEnum implements IDelimiter {
    R151, En_Tete_Flux, PRM, Numero_Abonnement, Id_PRM, Donnees_Releve, Date_Releve, Classe_Temporelle_Distributeur, Valeur, Puissance_Maximale;

    private byte[] tag;
    private byte[] tagStart;
    private byte[] tagEnd;

    private R151DelimiterEnum() {
        tag = name().getBytes();
        tagStart = ("<"+name()+">").getBytes();
        tagEnd = ("</"+name()+">").getBytes();
    }

    @Override
    public byte[] getTag() {
        return tag;
    }

    @Override
    public byte[] getTagStart() {
        return tagStart;
    }

    @Override
    public byte[] getTagEnd() {
        return tagEnd;
    }

}
