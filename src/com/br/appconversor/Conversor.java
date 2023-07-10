package com.br.appconversor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Conversor {
    private JFrame frame;
    private JPanel panel;
    private JComboBox<String> comboBoxTipo;
    private JComboBox<String> comboBoxOrigem;
    private JComboBox<String> comboBoxDestino;

    private static final String API_URL = "https://economia.awesomeapi.com.br/json/all";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Conversor conversor = new Conversor();
            conversor.criarInterfaceGrafica();
        });
    }

    private void criarInterfaceGrafica() {
        frame = new JFrame("Conversor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        panel = new JPanel();

        String[] opcoesTipo = {"Moedas", "Temperatura", "Quilometragem", "Anos-luz"};
        comboBoxTipo = new JComboBox<>(opcoesTipo);
        comboBoxTipo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tipoSelecionado = (String) comboBoxTipo.getSelectedItem();
                atualizarOpcoesConversao(tipoSelecionado);
            }
        });

        String[] opcoesMoedas = {"BRL - Real", "USD - Dólar", "EUR - Euro", "GBP - Libras Esterlinas", "ARS - Peso argentino", "CLP - Peso Chileno"};
        comboBoxOrigem = new JComboBox<>(opcoesMoedas);
        comboBoxDestino = new JComboBox<>(opcoesMoedas);

        JButton button = new JButton("Converter");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tipoSelecionado = (String) comboBoxTipo.getSelectedItem();
                String itemSelecionadoOrigem = (String) comboBoxOrigem.getSelectedItem();
                String itemSelecionadoDestino = (String) comboBoxDestino.getSelectedItem();

                String moedaOrigem = itemSelecionadoOrigem.split(" - ")[0];
                String moedaDestino = itemSelecionadoDestino.split(" - ")[0];

                if (moedaOrigem.equals(moedaDestino)) {
                    JOptionPane.showMessageDialog(frame, "Selecione moedas diferentes para a conversão.");
                } else {
                    if (tipoSelecionado.equals("Moedas")) {
                        if (moedaOrigem.equals("BRL")) {
                            double taxaConversao = obterTaxaConversao(moedaDestino);
                            if (taxaConversao != -1) {
                                converterMoedas(moedaOrigem, moedaDestino, taxaConversao);
                            } else {
                                JOptionPane.showMessageDialog(frame, "Não foi possível obter a taxa de conversão. Verifique sua conexão com a internet e tente novamente.");
                            }
                        } else {
                            JOptionPane.showMessageDialog(frame, "A conversão só é possível a partir do Real (BRL).");
                        }
                    } else if (tipoSelecionado.equals("Temperatura")) {
                        converterTemperatura();
                    } else if (tipoSelecionado.equals("Quilometragem")) {
                        converterQuilometragem();
                    } else if (tipoSelecionado.equals("Anos-luz")) {
                        converterAnosLuz();
                    }
                }
            }
        });

        panel.add(new JLabel("Tipo de Conversão:"));
        panel.add(comboBoxTipo);
        panel.add(new JLabel("De qual unidade:"));
        panel.add(comboBoxOrigem);
        panel.add(new JLabel("Para qual unidade:"));
        panel.add(comboBoxDestino);
        panel.add(button);
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private void atualizarOpcoesConversao(String tipoSelecionado) {
        comboBoxOrigem.removeAllItems();
        comboBoxDestino.removeAllItems();

        switch (tipoSelecionado) {
            case "Moedas":
                String[] opcoesMoedas = {"BRL - Real", "USD - Dólar", "EUR - Euro", "GBP - Libras Esterlinas", "ARS - Peso argentino", "CLP - Peso Chileno"};
                for (String opcao : opcoesMoedas) {
                    comboBoxOrigem.addItem(opcao);
                    comboBoxDestino.addItem(opcao);
                }
                break;
            case "Temperatura":
                String[] opcoesTemperatura = {"Celsius", "Fahrenheit", "Kelvin"};
                for (String opcao : opcoesTemperatura) {
                    comboBoxOrigem.addItem(opcao);
                    comboBoxDestino.addItem(opcao);
                }
                break;
            case "Quilometragem":
                String[] opcoesQuilometragem = {"Quilômetros", "Milhas"};
                for (String opcao : opcoesQuilometragem) {
                    comboBoxOrigem.addItem(opcao);
                    comboBoxDestino.addItem(opcao);
                }
                break;
            case "Anos-luz":
                String[] opcoesAnosLuz = {"Anos-luz", "Parsecs"};
                for (String opcao : opcoesAnosLuz) {
                    comboBoxOrigem.addItem(opcao);
                    comboBoxDestino.addItem(opcao);
                }
                break;
        }
    }

    private double obterTaxaConversao(String moedaDestino) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                if (jsonObject.has(moedaDestino)) {
                    JSONObject jsonDestino = jsonObject.getJSONObject(moedaDestino);

                    double taxaDestino = jsonDestino.getDouble("bid");

                    return taxaDestino;
                } else {
                    JOptionPane.showMessageDialog(frame, "Não foi possível obter a taxa de conversão para a moeda selecionada.");
                    return -1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private void converterMoedas(String moedaOrigem, String moedaDestino, double taxaConversao) {
        String valorStr = JOptionPane.showInputDialog(frame, "Digite o valor em " + moedaOrigem + ":");
        if (valorStr != null) {
            try {
                double valorMoedaOrigem = Double.parseDouble(valorStr.replace(",", "."));
                double valorMoedaDestino = valorMoedaOrigem * taxaConversao;

                DecimalFormat df = new DecimalFormat("#.00");

                String resultado= "Valor em " + moedaDestino + ": " + df.format(valorMoedaDestino);

                JOptionPane.showMessageDialog(frame, resultado);

                int escolha = JOptionPane.showConfirmDialog(frame, "Deseja realizar outra conversão?", "Escolha uma opção", JOptionPane.YES_NO_OPTION);
                if (escolha == JOptionPane.YES_OPTION) {
                    // Reiniciar o programa
                    comboBoxTipo.setSelectedIndex(0);
                    atualizarOpcoesConversao("Moedas");
                } else {
                    JOptionPane.showMessageDialog(frame, "Programa finalizado.");
                    System.exit(0);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Valor inválido!");
            }
        }
    }

    private void converterTemperatura() {
        String unidadeOrigem = (String) comboBoxOrigem.getSelectedItem();
        String unidadeDestino = (String) comboBoxDestino.getSelectedItem();

        double valorOrigem = obterValorEntrada("Digite a temperatura em " + unidadeOrigem + ":");

        double valorDestino;
        if (unidadeOrigem.equals("Celsius")) {
            valorDestino = converterCelsius(unidadeDestino, valorOrigem);
        } else if (unidadeOrigem.equals("Fahrenheit")) {
            valorDestino = converterFahrenheit(unidadeDestino, valorOrigem);
        } else { // Kelvin
            valorDestino = converterKelvin(unidadeDestino, valorOrigem);
        }

        String resultado = "Valor em " + unidadeDestino + ": " + valorDestino;

        JOptionPane.showMessageDialog(frame, resultado);

        int escolha = JOptionPane.showConfirmDialog(frame, "Deseja realizar outra conversão?", "Escolha uma opção", JOptionPane.YES_NO_OPTION);
        if (escolha == JOptionPane.YES_OPTION) {
            // Reiniciar o programa
            comboBoxTipo.setSelectedIndex(0);
            atualizarOpcoesConversao("Moedas");
        } else {
            JOptionPane.showMessageDialog(frame, "Programa finalizado.");
            System.exit(0);
        }
    }

    private double obterValorEntrada(String mensagem) {
        while (true) {
            String valorStr = JOptionPane.showInputDialog(frame, mensagem);
            if (valorStr != null) {
                try {
                    return Double.parseDouble(valorStr.replace(",", "."));
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Valor inválido!");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Programa finalizado.");
                System.exit(0);
            }
        }
    }

    private double converterCelsius(String unidadeDestino, double valorOrigem) {
        if (unidadeDestino.equals("Fahrenheit")) {
            return (valorOrigem * 9 / 5) + 32;
        } else if (unidadeDestino.equals("Kelvin")) {
            return valorOrigem + 273.15;
        } else { // Celsius
            return valorOrigem;
        }
    }

    private double converterFahrenheit(String unidadeDestino, double valorOrigem) {
        if (unidadeDestino.equals("Celsius")) {
            return (valorOrigem - 32) * 5 / 9;
        } else if (unidadeDestino.equals("Kelvin")) {
            return (valorOrigem + 459.67) * 5 / 9;
        } else { // Fahrenheit
            return valorOrigem;
        }
    }

    private double converterKelvin(String unidadeDestino, double valorOrigem) {
        if (unidadeDestino.equals("Celsius")) {
            return valorOrigem - 273.15;
        } else if (unidadeDestino.equals("Fahrenheit")) {
            return (valorOrigem * 9 / 5) - 459.67;
        } else { // Kelvin
            return valorOrigem;
        }
    }

    private void converterQuilometragem() {
        String unidadeOrigem = (String) comboBoxOrigem.getSelectedItem();
        String unidadeDestino = (String) comboBoxDestino.getSelectedItem();

        double valorOrigem = obterValorEntrada("Digite a quilometragem em " + unidadeOrigem + ":");

        double valorDestino;
        if (unidadeOrigem.equals("Quilômetros")) {
            valorDestino = converterQuilometros(unidadeDestino, valorOrigem);
        } else { // Milhas
            valorDestino = converterMilhas(unidadeDestino, valorOrigem);
        }

        String resultado = "Valor em " + unidadeDestino + ": " + valorDestino;

        JOptionPane.showMessageDialog(frame, resultado);

        int escolha = JOptionPane.showConfirmDialog(frame, "Deseja realizar outra conversão?", "Escolha uma opção", JOptionPane.YES_NO_OPTION);
        if (escolha == JOptionPane.YES_OPTION) {
            // Reiniciar o programa
            comboBoxTipo.setSelectedIndex(0);
            atualizarOpcoesConversao("Moedas");
        } else {
            JOptionPane.showMessageDialog(frame, "Programa finalizado.");
            System.exit(0);
        }
    }

    private double converterQuilometros(String unidadeDestino, double valorOrigem) {
        if (unidadeDestino.equals("Milhas")) {
            return valorOrigem * 0.621371;
        } else { // Quilômetros
            return valorOrigem;
        }
    }

    private double converterMilhas(String unidadeDestino, double valorOrigem) {
        if (unidadeDestino.equals("Quilômetros")) {
            return valorOrigem * 1.60934;
        } else { // Milhas
            return valorOrigem;
        }
    }

    private void converterAnosLuz() {
        String unidadeOrigem = (String) comboBoxOrigem.getSelectedItem();
        String unidadeDestino = (String) comboBoxDestino.getSelectedItem();

        double valorOrigem = obterValorEntrada("Digite a distância em " + unidadeOrigem + ":");

        double valorDestino;
        if (unidadeOrigem.equals("Anos-luz")) {
            valorDestino = converterAnosLuz(unidadeDestino, valorOrigem);
        } else { // Parsecs
            valorDestino = converterParsecs(unidadeDestino, valorOrigem);
        }

        String resultado = "Valor em " + unidadeDestino + ": " + valorDestino;

        JOptionPane.showMessageDialog(frame, resultado);

        int escolha = JOptionPane.showConfirmDialog(frame, "Deseja realizar outra conversão?", "Escolha uma opção", JOptionPane.YES_NO_OPTION);
        if (escolha == JOptionPane.YES_OPTION) {
            // Reiniciar o programa
            comboBoxTipo.setSelectedIndex(0);
            atualizarOpcoesConversao("Moedas");
        } else {
            JOptionPane.showMessageDialog(frame, "Programa finalizado.");
            System.exit(0);
        }
    }

    private double converterAnosLuz(String unidadeDestino, double valorOrigem) {
        if (unidadeDestino.equals("Parsecs")) {
            return valorOrigem * 0.306601;
        } else { // Anos-luz
            return valorOrigem;
        }
    }

    private double converterParsecs(String unidadeDestino, double valorOrigem) {
        if (unidadeDestino.equals("Anos-luz")) {
            return valorOrigem * 3.26156;
        } else { // Parsecs
            return valorOrigem;
        }
    }

}
