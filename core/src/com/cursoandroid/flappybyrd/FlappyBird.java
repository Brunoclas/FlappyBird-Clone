package com.cursoandroid.flappybyrd;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture[] passaros;
    private Texture fundo;
    private Texture cano_baixo;
    private Texture cano_topo;
    private Texture game_over;
    private Random numero_randomico;
    private BitmapFont fonte, msg;
    private Circle passaro_circulo;
    private Rectangle retangulo_cano_topo;
    private Rectangle retangulo_cano_baixo;
    // private ShapeRenderer shape;

    // Atributos de configuração
    private float largura_dispositivo, altura_dispositivo;
    private int estado_jogo = 0; // 0 -> Jogo não iniciado; 1 -> Jogo iniciado; -> 2 Game Over
    private int pontuacao = 0;

    private float variacao = 0;
    private float velocidade_queda = 0;
    private float posicacao_inicial_vertical;
    private float posicao_movimento_cano_horizontal;
    private float espaco_entre_canos;
    private float delta_time;
    private float altura_entre_canos_randomicos;
    private boolean marcou_ponto = false;

    //Camera
    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 768;
    private final float VIRTUAL_HEIGHT = 1024;

    @Override
    public void create() {
        batch = new SpriteBatch();
        numero_randomico = new Random();
        passaro_circulo = new Circle();
       /* retangulo_cano_baixo = new Rectangle();
        retangulo_cano_topo = new Rectangle();
        shape = new ShapeRenderer();*/
        fonte = new BitmapFont();
        fonte.setColor(Color.WHITE);
        fonte.getData().setScale(6);

        msg = new BitmapFont();
        msg.setColor(Color.WHITE);
        msg.getData().setScale(3);

        passaros = new Texture[3];
        passaros[0] = new Texture("passaro1.png");
        passaros[1] = new Texture("passaro2.png");
        passaros[2] = new Texture("passaro3.png");
        fundo = new Texture("fundo.png");
        cano_baixo = new Texture("cano_baixo_maior.png");
        cano_topo = new Texture("cano_topo_maior.png");
        game_over = new Texture("game_over.png");

        /******************************************************
         * Configurações da camera
         */
        camera = new OrthographicCamera();

        camera.position.set(VIRTUAL_WIDTH / 2,
                VIRTUAL_HEIGHT / 2,
                0);

        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        largura_dispositivo = VIRTUAL_WIDTH;
        altura_dispositivo = VIRTUAL_HEIGHT;
        posicacao_inicial_vertical = altura_dispositivo / 2 + espaco_entre_canos / 2;
        posicao_movimento_cano_horizontal = largura_dispositivo;
        espaco_entre_canos = 500;

    }

    @Override
    public void render() {

        camera.update();

        //Limpar frames anteriores
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        delta_time = Gdx.graphics.getDeltaTime();
        variacao += delta_time * 10;
        if (variacao > 2) variacao = 0;

        if (estado_jogo == 0) { //Não iniciado
            if (Gdx.input.justTouched()) {
                estado_jogo = 1;
            }
        } else { // Jogo Iniciado

            velocidade_queda++;
            if (posicacao_inicial_vertical > 0 || velocidade_queda < 0)
                posicacao_inicial_vertical = posicacao_inicial_vertical - velocidade_queda;

            if (estado_jogo == 1) {

                posicao_movimento_cano_horizontal -= delta_time * 200;

                if (Gdx.input.justTouched()) {
                    velocidade_queda = -15;
                }
                // Verifica se o cano saiu inteiramente da tela.
                if (posicao_movimento_cano_horizontal < -cano_topo.getWidth()) {
                    posicao_movimento_cano_horizontal = largura_dispositivo;
                    altura_entre_canos_randomicos = numero_randomico.nextInt(400) - 200;
                    marcou_ponto = false;
                }

                // Verifica pontuação
                if (posicao_movimento_cano_horizontal < 120) {
                    if (!marcou_ponto) {
                        pontuacao++;
                        marcou_ponto = true;
                    }

                }
            } else { // Tela Geme over
                if (Gdx.input.justTouched()) {
                    estado_jogo = 0;
                    pontuacao = 0;
                    velocidade_queda = 0;
                    posicacao_inicial_vertical = altura_dispositivo / 2;
                    posicao_movimento_cano_horizontal = largura_dispositivo;
                }
            }
        }

        //Configurar dados de projeção da camera
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // Introduz o fundo do cenario
        batch.draw(fundo,
                0,
                0,
                largura_dispositivo,
                altura_dispositivo);

        // Introduz o cano do topo no cenrio
        batch.draw(cano_topo,
                posicao_movimento_cano_horizontal,
                altura_dispositivo / 2 + altura_entre_canos_randomicos);

        // Introduz o cano de baixo no cenario
        batch.draw(cano_baixo,
                posicao_movimento_cano_horizontal,
                altura_dispositivo / 2 - cano_baixo.getHeight() - espaco_entre_canos / 2 + altura_entre_canos_randomicos);

        // Introduz o passaro no cenario
        batch.draw(passaros[(int) variacao],
                120,
                posicacao_inicial_vertical);

        // Introduz a fonte q informa a pontuação no cenario
        fonte.draw(batch,
                String.valueOf(pontuacao),
                largura_dispositivo / 2,
                altura_dispositivo - 50);

        // Introduz a imagem de game Over
        if (estado_jogo == 2) {
            batch.draw(game_over,
                    largura_dispositivo / 2 - game_over.getWidth() / 2,
                    altura_dispositivo / 2);

            // Introduz a mensagem para reiniciar o jogo
            msg.draw(batch,
                    "Toque para reiniciar!",
                    largura_dispositivo / 2 - 200,
                    altura_dispositivo / 2 - game_over.getHeight() / 2);
        }

        batch.end();

        passaro_circulo.set(
                120 + passaros[0].getWidth() / 2,
                posicacao_inicial_vertical + passaros[0].getHeight() / 2,
                passaros[0].getWidth() / 2);

        retangulo_cano_baixo = new Rectangle(
                posicao_movimento_cano_horizontal,
                altura_dispositivo / 2 - cano_baixo.getHeight() - espaco_entre_canos / 2 + altura_entre_canos_randomicos,
                cano_baixo.getWidth(),
                cano_baixo.getHeight()
        );

        retangulo_cano_topo = new Rectangle(
                posicao_movimento_cano_horizontal,
                altura_dispositivo / 2 + altura_entre_canos_randomicos,
                cano_topo.getWidth(),
                cano_topo.getHeight()
        );

        //Desenhar Formas
       /* shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.circle(passaro_circulo.x, passaro_circulo.y, passaro_circulo.radius);
        shape.rect(retangulo_cano_baixo.x, retangulo_cano_baixo.y, retangulo_cano_baixo.width, retangulo_cano_baixo.height);
        shape.rect(retangulo_cano_topo.x, retangulo_cano_topo.y, retangulo_cano_topo.width, retangulo_cano_topo.height);
        shape.setColor(Color.RED);
        shape.end(); */

        //Teste de colisão
        if (Intersector.overlaps(passaro_circulo, retangulo_cano_baixo)
                || Intersector.overlaps(passaro_circulo, retangulo_cano_topo)
                || posicacao_inicial_vertical <= 0
                || posicacao_inicial_vertical >= altura_dispositivo) {
            estado_jogo = 2;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

}
